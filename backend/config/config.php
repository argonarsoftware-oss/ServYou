<?php

// CORS headers for Android app
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");
header("Content-Type: application/json; charset=UTF-8");

// Handle preflight
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// Email settings — update these for your VPS SMTP
define('MAIL_FROM',      getenv('MAIL_FROM')      ?: 'noreply@servyou.app');
define('MAIL_FROM_NAME', getenv('MAIL_FROM_NAME') ?: 'ServYou Booking');
define('SMTP_HOST',      getenv('SMTP_HOST')      ?: 'localhost');
define('SMTP_PORT',      getenv('SMTP_PORT')      ?: '25');

// API key for basic auth (set this in your environment)
define('API_KEY', getenv('SERVYOU_API_KEY') ?: 'changeme-servyou-2026');

require_once __DIR__ . '/database.php';
