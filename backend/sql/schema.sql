CREATE DATABASE IF NOT EXISTS servyou_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE servyou_db;

CREATE TABLE IF NOT EXISTS bookings (
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_status ON bookings(status);
CREATE INDEX idx_date ON bookings(date);
CREATE INDEX idx_created_at ON bookings(created_at);
