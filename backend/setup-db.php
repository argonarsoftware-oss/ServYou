<?php
/**
 * ServYou Database Setup
 * Visit this page once to create the database and tables.
 * DELETE THIS FILE after setup is complete.
 */

$host = 'localhost';
$user = 'root';
$pass = ''; // Change this to your MySQL root password

header('Content-Type: text/html; charset=UTF-8');

echo "<!DOCTYPE html><html><head><title>ServYou DB Setup</title>
<style>
body{font-family:monospace;background:#1a1a2e;color:#e0e0e0;padding:40px;max-width:700px;margin:auto}
h1{color:#C8A951}
.ok{color:#4CAF50;font-weight:bold}
.err{color:#F44336;font-weight:bold}
.step{background:#16213e;border-left:4px solid #C8A951;padding:12px 16px;margin:10px 0;border-radius:6px}
.warn{background:#2d1b00;border-left:4px solid #F57C00;padding:12px 16px;margin:20px 0;border-radius:6px}
</style></head><body><h1>ServYou - Database Setup</h1>";

try {
    // Connect without database
    $pdo = new PDO("mysql:host=$host", $user, $pass, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION
    ]);
    echo '<div class="step"><span class="ok">✓</span> Connected to MySQL</div>';

    // Create database
    $pdo->exec("CREATE DATABASE IF NOT EXISTS servyou_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
    echo '<div class="step"><span class="ok">✓</span> Database <b>servyou_db</b> created</div>';

    // Select database
    $pdo->exec("USE servyou_db");
    echo '<div class="step"><span class="ok">✓</span> Selected servyou_db</div>';

    // Create bookings table
    $pdo->exec("CREATE TABLE IF NOT EXISTS bookings (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        customer_name VARCHAR(255) NOT NULL,
        contact_number VARCHAR(50) NOT NULL,
        email VARCHAR(255) NOT NULL,
        service VARCHAR(255) NOT NULL,
        date VARCHAR(50) NOT NULL,
        time VARCHAR(50) NOT NULL,
        notes TEXT DEFAULT '',
        status ENUM('Pending', 'Confirmed', 'Completed', 'Cancelled') DEFAULT 'Pending',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        INDEX idx_status (status),
        INDEX idx_date (date),
        INDEX idx_created_at (created_at)
    ) ENGINE=InnoDB");
    echo '<div class="step"><span class="ok">✓</span> Table <b>bookings</b> created</div>';

    // Verify
    $stmt = $pdo->query("SHOW TABLES");
    $tables = $stmt->fetchAll(PDO::FETCH_COLUMN);
    echo '<div class="step"><span class="ok">✓</span> Tables in servyou_db: <b>' . implode(', ', $tables) . '</b></div>';

    $stmt = $pdo->query("DESCRIBE bookings");
    $cols = $stmt->fetchAll(PDO::FETCH_COLUMN);
    echo '<div class="step"><span class="ok">✓</span> Columns: <b>' . implode(', ', $cols) . '</b></div>';

    echo '<div class="step" style="border-left-color:#4CAF50"><span class="ok">✓ ALL DONE — Database setup complete!</span></div>';
    echo '<div class="warn"><b>⚠ IMPORTANT:</b> Delete this file from your server now!<br><code>rm /var/www/ServYou/backend/setup-db.php</code></div>';

} catch (PDOException $e) {
    echo '<div class="step"><span class="err">✗ Error:</span> ' . htmlspecialchars($e->getMessage()) . '</div>';
    echo '<div class="warn"><b>Fix:</b> Edit the <code>$pass</code> variable at the top of this file to match your MySQL root password.</div>';
}

echo "</body></html>";
