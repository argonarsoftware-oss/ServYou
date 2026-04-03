<?php
/**
 * ServYou API Router
 *
 * Endpoints:
 *   GET    /api/bookings          — list all bookings
 *   GET    /api/bookings/{id}     — get single booking
 *   POST   /api/bookings          — create booking
 *   PUT    /api/bookings/{id}     — update booking
 *   PATCH  /api/bookings/{id}     — update status only
 *   DELETE /api/bookings/{id}     — delete booking
 *   GET    /api/bookings/stats    — get booking counts
 */

require_once __DIR__ . '/../config/config.php';

// Simple API key check
function authenticate(): void {
    $headers = getallheaders();
    $key = $headers['Authorization'] ?? $headers['authorization'] ?? '';
    $key = str_replace('Bearer ', '', $key);
    if ($key !== API_KEY) {
        http_response_code(401);
        echo json_encode(['error' => 'Unauthorized']);
        exit;
    }
}

authenticate();

$db   = (new Database())->getConnection();
$uri  = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$uri  = rtrim(str_replace('/api', '', $uri), '/');
$method = $_SERVER['REQUEST_METHOD'];

// Route: /bookings/stats
if ($uri === '/bookings/stats' && $method === 'GET') {
    $stmt = $db->query("SELECT
        COUNT(*) as total,
        SUM(status = 'Pending') as pending,
        SUM(status = 'Confirmed') as confirmed,
        SUM(status = 'Completed') as completed,
        SUM(status = 'Cancelled') as cancelled
        FROM bookings");
    echo json_encode(['success' => true, 'data' => $stmt->fetch()]);
    exit;
}

// Route: /bookings or /bookings/{id}
if (preg_match('#^/bookings(?:/(\d+))?$#', $uri, $matches)) {
    $id = $matches[1] ?? null;

    switch ($method) {
        case 'GET':
            if ($id) {
                // Single booking
                $stmt = $db->prepare("SELECT * FROM bookings WHERE id = ?");
                $stmt->execute([$id]);
                $booking = $stmt->fetch();
                if (!$booking) {
                    http_response_code(404);
                    echo json_encode(['error' => 'Booking not found']);
                } else {
                    echo json_encode(['success' => true, 'data' => $booking]);
                }
            } else {
                // List all
                $page  = max(1, (int)($_GET['page'] ?? 1));
                $limit = min(100, max(1, (int)($_GET['limit'] ?? 50)));
                $offset = ($page - 1) * $limit;

                $where = '';
                $params = [];
                if (!empty($_GET['status'])) {
                    $where = 'WHERE status = ?';
                    $params[] = $_GET['status'];
                }

                $countStmt = $db->prepare("SELECT COUNT(*) FROM bookings $where");
                $countStmt->execute($params);
                $total = (int)$countStmt->fetchColumn();

                $stmt = $db->prepare("SELECT * FROM bookings $where ORDER BY created_at DESC LIMIT ? OFFSET ?");
                $params[] = $limit;
                $params[] = $offset;
                $stmt->execute($params);

                echo json_encode([
                    'success' => true,
                    'data'    => $stmt->fetchAll(),
                    'total'   => $total,
                    'page'    => $page,
                    'limit'   => $limit
                ]);
            }
            break;

        case 'POST':
            $input = json_decode(file_get_contents('php://input'), true);
            if (!$input) {
                http_response_code(400);
                echo json_encode(['error' => 'Invalid JSON']);
                exit;
            }

            $required = ['customer_name', 'contact_number', 'email', 'service', 'date', 'time'];
            foreach ($required as $field) {
                if (empty($input[$field])) {
                    http_response_code(422);
                    echo json_encode(['error' => "Field '$field' is required"]);
                    exit;
                }
            }

            $stmt = $db->prepare("INSERT INTO bookings
                (customer_name, contact_number, email, service, date, time, notes, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')");
            $stmt->execute([
                $input['customer_name'],
                $input['contact_number'],
                $input['email'],
                $input['service'],
                $input['date'],
                $input['time'],
                $input['notes'] ?? ''
            ]);

            $newId = $db->lastInsertId();

            // Send confirmation email
            sendConfirmationEmail($input);

            http_response_code(201);
            echo json_encode(['success' => true, 'id' => (int)$newId, 'message' => 'Booking created']);
            break;

        case 'PUT':
            if (!$id) {
                http_response_code(400);
                echo json_encode(['error' => 'Booking ID required']);
                exit;
            }

            $input = json_decode(file_get_contents('php://input'), true);
            if (!$input) {
                http_response_code(400);
                echo json_encode(['error' => 'Invalid JSON']);
                exit;
            }

            $stmt = $db->prepare("UPDATE bookings SET
                customer_name = ?, contact_number = ?, email = ?,
                service = ?, date = ?, time = ?, notes = ?, status = ?
                WHERE id = ?");
            $stmt->execute([
                $input['customer_name'],
                $input['contact_number'],
                $input['email'],
                $input['service'],
                $input['date'],
                $input['time'],
                $input['notes'] ?? '',
                $input['status'] ?? 'Pending',
                $id
            ]);

            echo json_encode(['success' => true, 'message' => 'Booking updated']);
            break;

        case 'PATCH':
            if (!$id) {
                http_response_code(400);
                echo json_encode(['error' => 'Booking ID required']);
                exit;
            }

            $input = json_decode(file_get_contents('php://input'), true);
            $status = $input['status'] ?? null;
            $allowed = ['Pending', 'Confirmed', 'Completed', 'Cancelled'];

            if (!$status || !in_array($status, $allowed)) {
                http_response_code(422);
                echo json_encode(['error' => 'Valid status required: ' . implode(', ', $allowed)]);
                exit;
            }

            $stmt = $db->prepare("UPDATE bookings SET status = ? WHERE id = ?");
            $stmt->execute([$status, $id]);

            echo json_encode(['success' => true, 'message' => "Status updated to $status"]);
            break;

        case 'DELETE':
            if (!$id) {
                http_response_code(400);
                echo json_encode(['error' => 'Booking ID required']);
                exit;
            }

            $stmt = $db->prepare("DELETE FROM bookings WHERE id = ?");
            $stmt->execute([$id]);

            echo json_encode(['success' => true, 'message' => 'Booking deleted']);
            break;

        default:
            http_response_code(405);
            echo json_encode(['error' => 'Method not allowed']);
    }
    exit;
}

http_response_code(404);
echo json_encode(['error' => 'Endpoint not found']);

/**
 * Send booking confirmation email via PHP mail()
 */
function sendConfirmationEmail(array $booking): void {
    $to      = $booking['email'];
    $subject = "ServYou - Booking Confirmation";

    $body = "
    <html>
    <body style='font-family: Arial, sans-serif; background: #FFF8F0; padding: 20px;'>
        <div style='max-width: 500px; margin: auto; background: white; border-radius: 16px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>
            <h1 style='color: #795548; text-align: center;'>ServYou</h1>
            <p style='color: #C8A951; text-align: center;'>Salon &bull; Spa &bull; Clinic</p>
            <hr style='border: 1px solid #D7CCC8;'>
            <h2 style='color: #3E2723;'>Booking Confirmed!</h2>
            <p><strong>Name:</strong> {$booking['customer_name']}</p>
            <p><strong>Service:</strong> {$booking['service']}</p>
            <p><strong>Date:</strong> {$booking['date']}</p>
            <p><strong>Time:</strong> {$booking['time']}</p>
            <p><strong>Contact:</strong> {$booking['contact_number']}</p>
            <hr style='border: 1px solid #D7CCC8;'>
            <p style='color: #5D4037; font-size: 13px; text-align: center;'>
                Thank you for booking with ServYou!<br>
                We will confirm your appointment shortly.
            </p>
        </div>
    </body>
    </html>";

    $headers  = "MIME-Version: 1.0\r\n";
    $headers .= "Content-type: text/html; charset=UTF-8\r\n";
    $headers .= "From: " . MAIL_FROM_NAME . " <" . MAIL_FROM . ">\r\n";

    @mail($to, $subject, $body, $headers);
}
