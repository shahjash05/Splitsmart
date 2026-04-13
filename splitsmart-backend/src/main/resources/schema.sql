-- SplitSmart MySQL Schema
-- Run this in MySQL Workbench before starting the Spring Boot application

CREATE DATABASE IF NOT EXISTS splitsmart;
USE splitsmart;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    monthly_spending_limit DECIMAL(10,2) DEFAULT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    category_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    icon          VARCHAR(10)
);

-- Groups table
CREATE TABLE IF NOT EXISTS `groups` (
    group_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_name    VARCHAR(150) NOT NULL,
    created_by    BIGINT NOT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- Group Memberships (junction table)
CREATE TABLE IF NOT EXISTS group_memberships (
    membership_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    group_id      BIGINT NOT NULL,
    UNIQUE KEY uq_user_group (user_id, group_id),
    FOREIGN KEY (user_id)  REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES `groups`(group_id) ON DELETE CASCADE
);

-- Expenses table
CREATE TABLE IF NOT EXISTS expenses (
    expense_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    description     VARCHAR(255) NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    date_of_expense DATE NOT NULL,
    created_by      BIGINT NOT NULL,
    category_id     BIGINT NOT NULL,
    group_id        BIGINT DEFAULT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by)  REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (group_id)    REFERENCES `groups`(group_id) ON DELETE SET NULL
);

-- Expense Participations (who paid/owes what in each expense)
CREATE TABLE IF NOT EXISTS expense_participations (
    participation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expense_id      BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    owed_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    paid_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT chk_owed_non_negative CHECK (owed_amount >= 0),
    CONSTRAINT chk_paid_non_negative CHECK (paid_amount >= 0),
    FOREIGN KEY (expense_id) REFERENCES expenses(expense_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)    REFERENCES users(user_id)
);

-- Settlements table
CREATE TABLE IF NOT EXISTS settlements (
    settlement_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    payer_id      BIGINT NOT NULL,
    receiver_id   BIGINT NOT NULL,
    amount        DECIMAL(10,2) NOT NULL,
    status        ENUM('PENDING','CONFIRMED','REJECTED') DEFAULT 'PENDING',
    group_id      BIGINT DEFAULT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payer_neq_receiver CHECK (payer_id <> receiver_id),
    FOREIGN KEY (payer_id)    REFERENCES users(user_id),
    FOREIGN KEY (receiver_id) REFERENCES users(user_id),
    FOREIGN KEY (group_id)    REFERENCES `groups`(group_id) ON DELETE SET NULL
);

-- Seed Categories
INSERT IGNORE INTO categories (category_id, category_name, icon) VALUES
(1, 'Food & Dining'),
(2, 'Transportation'),
(3, 'Entertainment'),
(4, 'Utilities'),
(5, 'Shopping'),
(6, 'Health'),
(7, 'Travel'),
(8, 'Other');
