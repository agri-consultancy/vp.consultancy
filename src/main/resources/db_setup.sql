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
    sector VARCHAR(100) COMMENT 'Primary sector: Arable, Horticulture, Mixed, etc.',
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

-- ==================== CROP VARIETIES TABLE ====================
-- Stores supported crop varieties defined by consultants
CREATE TABLE IF NOT EXISTS crop_varieties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crop_id BIGINT,
    consultant_id BIGINT,
    name VARCHAR(255) NOT NULL COMMENT 'Variety name like Golden Harvest X-12',
    description VARCHAR(500) COMMENT 'Specific traits and growth habits',
    climate VARCHAR(255) COMMENT 'Climate preference: Tropical, Temperate, Arid, Subtropical',
    yield_potential VARCHAR(255) COMMENT 'Typical yield like 4.5-5.2 Tons/Hectare',
    cycle_duration_days BIGINT COMMENT 'Days for crop cycle',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (crop_id) REFERENCES crops(id) ON DELETE SET NULL,
    FOREIGN KEY (consultant_id) REFERENCES user_profiles(id) ON DELETE SET NULL,
    INDEX idx_crop_id (crop_id),
    INDEX idx_consultant_id (consultant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Master table for crop varieties';

-- ==================== FARMER CROP VARIETIES TABLE ====================
-- Tracks crop varieties assigned to farmers with cultivation details
CREATE TABLE IF NOT EXISTS farmer_crop_varieties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT NOT NULL COMMENT 'Reference to farmer user profile',
    crop_variety_id BIGINT NOT NULL COMMENT 'Reference to crop variety',
    total_land DOUBLE COMMENT 'Total land area in hectares',
    total_plants INT COMMENT 'Total number of plants',
    sowing_date DATE COMMENT 'Date of sowing',
    expected_harvest_date DATE COMMENT 'Expected harvest date',
    status VARCHAR(50) COMMENT 'Status: Preparing, Active, Harvesting, Completed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (crop_variety_id) REFERENCES crop_varieties(id) ON DELETE CASCADE,
    INDEX idx_farmer_id (farmer_id),
    INDEX idx_crop_variety_id (crop_variety_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Farmer crop variety assignments and cultivation tracking';

-- ==================== FARMER SENT SCHEDULE TABLES ====================
-- Tracks consultant-sent schedule batches and frozen day/task snapshots
CREATE TABLE IF NOT EXISTS farmer_crop_variety_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT NOT NULL COMMENT 'Reference to farmer user profile',
    farmer_crop_variety_id BIGINT NOT NULL COMMENT 'Reference to farmer crop variety assignment',
    start_date DATE COMMENT 'Date when this schedule batch was sent',
    last_sent_day BIGINT NOT NULL COMMENT 'Last day number included in this batch',
    last_sent_master_day BIGINT COMMENT 'Last master schedule day included in this batch',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (farmer_crop_variety_id) REFERENCES farmer_crop_varieties(id) ON DELETE CASCADE,
    INDEX idx_fcv_schedule_farmer (farmer_id),
    INDEX idx_fcv_schedule_variety (farmer_crop_variety_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Sent schedule batches for farmer crop assignments';

CREATE TABLE IF NOT EXISTS farmer_schedule_gaps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT NOT NULL COMMENT 'Reference to farmer user profile',
    farmer_crop_variety_id BIGINT NOT NULL COMMENT 'Reference to farmer crop variety assignment',
    gap_days BIGINT NOT NULL COMMENT 'Number of farmer-side empty days inserted before the next master day mapping',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (farmer_crop_variety_id) REFERENCES farmer_crop_varieties(id) ON DELETE CASCADE,
    INDEX idx_farmer_schedule_gap_farmer (farmer_id),
    INDEX idx_farmer_schedule_gap_variety (farmer_crop_variety_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Farmer-side gap history used to offset master schedule day mapping';

CREATE TABLE IF NOT EXISTS farmer_schedule_days (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL COMMENT 'Reference to sent schedule batch',
    farmer_id BIGINT NOT NULL COMMENT 'Reference to farmer user profile',
    farmer_crop_variety_id BIGINT NOT NULL COMMENT 'Reference to farmer crop variety assignment',
    day_number BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(50),
    display_order BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES farmer_crop_variety_schedule(id) ON DELETE CASCADE,
    FOREIGN KEY (farmer_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (farmer_crop_variety_id) REFERENCES farmer_crop_varieties(id) ON DELETE CASCADE,
    INDEX idx_farmer_schedule_day_farmer (farmer_id),
    INDEX idx_farmer_schedule_day_variety (farmer_crop_variety_id),
    INDEX idx_farmer_schedule_day_number (day_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Day-wise sent schedules for a farmer crop assignment';

CREATE TABLE IF NOT EXISTS farmer_schedule_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_day_id BIGINT NOT NULL COMMENT 'Reference to farmer_schedule_days',
    fertilizer_name VARCHAR(255) NOT NULL,
    quantity VARCHAR(255) NOT NULL,
    proportion VARCHAR(50) NOT NULL,
    priority BIGINT,
    description VARCHAR(255) NOT NULL,
    task_type VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_day_id) REFERENCES farmer_schedule_days(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Task-level schedule details for each farmer schedule day';

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

-- ==================== CROPS TABLE ====================
-- Stores supported crops for consultancy services
CREATE TABLE IF NOT EXISTS crops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE COMMENT 'Crop name',
    description VARCHAR(500) COMMENT 'Crop description and details',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Master table for supported crops';

CREATE TRIGGER IF NOT EXISTS crops_update_timestamp
BEFORE UPDATE ON crops
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$




CREATE TABLE IF NOT EXISTS master_schedule_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consultant_id BIGINT NOT NULL,
    crop_variety_id BIGINT NOT NULL,
    version BIGINT,
    description VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (consultant_id) REFERENCES user_profiles(id) ON DELETE SET NULL,
    FOREIGN KEY (crop_variety_id) REFERENCES crop_varieties(id) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS master_schedule_days (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    day_number BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    display_order BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES master_schedule_templates(id) ON DELETE SET NULL
);



CREATE TABLE IF NOT EXISTS master_schedule_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_day_id BIGINT NOT NULL,
    fertilizer_name VARCHAR(255) NOT NULL,
    quantity VARCHAR(255) NOT NULL,
    proportion VARCHAR(50) NOT NULL,
    priority BIGINT,
    description VARCHAR(255) NOT NULL,
    task_type VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_day_id) REFERENCES master_schedule_days(id) ON DELETE SET NULL
);



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
