<?php

class Database {
    private string $host;
    private string $dbName;
    private string $username;
    private string $password;
    private ?PDO $conn = null;

    public function __construct() {
        $this->host     = getenv('DB_HOST') ?: 'localhost';
        $this->dbName   = getenv('DB_NAME') ?: 'servyou_db';
        $this->username = getenv('DB_USER') ?: 'root';
        $this->password = getenv('DB_PASS') ?: '';
    }

    public function getConnection(): PDO {
        if ($this->conn === null) {
            $dsn = "mysql:host={$this->host};dbname={$this->dbName};charset=utf8mb4";
            $this->conn = new PDO($dsn, $this->username, $this->password, [
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES   => false,
            ]);
        }
        return $this->conn;
    }
}
