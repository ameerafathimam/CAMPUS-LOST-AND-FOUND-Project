-- =====================================================================
-- Campus Lost and Found Management System - Database Schema
-- ---------------------------------------------------------------------
-- Run this file once as a MySQL administrator (root), e.g.:
--     mysql -u root -p < database/schema.sql
-- or open it in MySQL Workbench and execute.
--
-- Re-running this file is safe: it only creates the database and table
-- if they do not already exist. Existing records are NOT deleted.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS campus_lost_found
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_lost_found;

-- ---------------------------------------------------------------------
-- items: one row per lost-or-found report.
-- item_id is generated automatically by MySQL (AUTO_INCREMENT).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS items (
    item_id      INT            NOT NULL AUTO_INCREMENT,
    item_name    VARCHAR(100)   NOT NULL,
    category     VARCHAR(50)    NOT NULL,
    description  VARCHAR(500)   NULL,
    report_type  VARCHAR(10)    NOT NULL,
    location     VARCHAR(100)   NOT NULL,
    report_date  DATE           NOT NULL,
    contact_info VARCHAR(100)   NULL,
    status       VARCHAR(10)    NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (item_id),
    -- Restrict values at the database level (not only in Java code)
    CONSTRAINT chk_items_report_type CHECK (report_type IN ('LOST', 'FOUND')),
    CONSTRAINT chk_items_status      CHECK (status      IN ('ACTIVE', 'RECOVERED')),
    -- Indexes speed up the searches performed by the application
    INDEX idx_items_name     (item_name),
    INDEX idx_items_category (category),
    INDEX idx_items_type     (report_type),
    INDEX idx_items_status   (status),
    INDEX idx_items_location (location)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Optional: create the limited database user used by the application.
-- Uncomment and change the password before running as root.
-- The application never connects as root; it only gets rights on this
-- one database.
-- ---------------------------------------------------------------------
-- CREATE USER IF NOT EXISTS 'campus_app'@'localhost' IDENTIFIED BY 'change_this_password';
-- GRANT ALL PRIVILEGES ON campus_lost_found.* TO 'campus_app'@'localhost';
-- FLUSH PRIVILEGES;
