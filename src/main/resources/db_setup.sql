-- MySQL Database Script for VP Consultancy Application
-- Database: vp_consultancy
-- Created with enterprise-grade architecture and security best practices

-- Create Database
CREATE DATABASE IF NOT EXISTS vp_consultancy;
USE vp_consultancy;

-- ==================== ADDRESS TABLE ====================
-- Stores address information - reusable for multiple entities
CREATE TABLE IF NOT EXISTS address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    address_line VARCHAR(255) NOT NULL COMMENT 'Street address',
    city VARCHAR(100) NOT NULL COMMENT 'City name',
    district VARCHAR(100) COMMENT 'District name',
    state VARCHAR(100) NOT NULL COMMENT 'State/Province',
    postal_code VARCHAR(20) NOT NULL COMMENT 'Postal/ZIP code',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_city_state (city, state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Address master table for user locations';

-- ==================== USERS TABLE ====================
-- Core user table - contains authentication and role information
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mobile VARCHAR(13) NOT NULL UNIQUE COMMENT 'Mobile number (unique identifier)',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    role VARCHAR(50) NOT NULL COMMENT 'User role: ADMIN, CONSULTANT, FARMER',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT 'User status: ACTIVE, INACTIVE, SUSPENDED',
    is_mobile_verified BOOLEAN DEFAULT FALSE COMMENT 'Mobile verification status',
    is_email_verified BOOLEAN DEFAULT FALSE COMMENT 'Email verification status',
    failed_login_attempts BIGINT DEFAULT 0 COMMENT 'Track failed login attempts for security',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_mobile (mobile),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Core user authentication and role table';

-- ==================== USER PROFILES TABLE ====================
-- Stores detailed user profile information
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL COMMENT 'First name of user',
    last_name VARCHAR(255) NOT NULL COMMENT 'Last name of user',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT 'Email address (unique)',
    user_id BIGINT NOT NULL UNIQUE COMMENT 'Reference to users table (1:1 relationship)',
    consultant_id BIGINT COMMENT 'Reference to consultant user if this user is a farmer',
    address_id BIGINT COMMENT 'Reference to address table',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (consultant_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (address_id) REFERENCES address(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_consultant_id (consultant_id),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Extended user profile information';

-- ==================== REFRESH TOKEN TABLE ====================
-- Stores JWT refresh tokens for token rotation
CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE COMMENT 'JWT refresh token',
    expiry_date TIMESTAMP NOT NULL COMMENT 'Token expiration time',
    user_id BIGINT NOT NULL COMMENT 'Reference to user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'JWT refresh token storage for token rotation and revocation';

-- ==================== DEFAULT DATA ====================
-- Insert default ADMIN user for system initialization
-- Mobile: 9999999999, Password: admin (BCrypt hashed)
INSERT INTO users (mobile, password, role, status, is_mobile_verified, is_email_verified) 
VALUES ('9999999999', '$2a$10$slYQmyNdGzin7olVN3p5Be7DlH.PKZbv5H8KnzzVgXXbVxzy6xlzm', 'ADMIN', 'ACTIVE', TRUE, TRUE)
ON DUPLICATE KEY UPDATE id=id;

-- Insert admin profile
INSERT INTO user_profiles (first_name, last_name, email, user_id)
SELECT 'System', 'Admin', 'admin@vpconsultancy.com', id FROM users WHERE mobile = '9999999999' AND NOT EXISTS (SELECT 1 FROM user_profiles WHERE user_id = (SELECT id FROM users WHERE mobile = '9999999999'))
ON DUPLICATE KEY UPDATE id=id;

-- ==================== TRIGGERS ====================
-- Auto-update timestamp
DELIMITER $$
CREATE TRIGGER IF NOT EXISTS users_update_timestamp
BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$

CREATE TRIGGER IF NOT EXISTS user_profiles_update_timestamp
BEFORE UPDATE ON user_profiles
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$

CREATE TRIGGER IF NOT EXISTS address_update_timestamp
BEFORE UPDATE ON address
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$
DELIMITER ;
