-- Database initialization script for library borrowing analysis system
-- MySQL 8+

DROP DATABASE IF EXISTS library_borrowing;
CREATE DATABASE library_borrowing DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_borrowing;

-- Reader table
DROP TABLE IF EXISTS reader;
CREATE TABLE reader (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type ENUM('STUDENT', 'TEACHER', 'STAFF') NOT NULL DEFAULT 'STUDENT',
    department VARCHAR(100) NULL,
    email VARCHAR(150) NULL,
    phone VARCHAR(30) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Book table
DROP TABLE IF EXISTS book;
CREATE TABLE book (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,
    isbn VARCHAR(30) NOT NULL UNIQUE,
    publish_year INT NULL,
    total_copies INT NOT NULL DEFAULT 10,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Borrow record table
DROP TABLE IF EXISTS borrow_record;
CREATE TABLE borrow_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reader_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_time DATETIME NOT NULL,
    due_time DATETIME NOT NULL,
    return_time DATETIME NULL,
    status ENUM('BORROWED', 'RETURNED', 'OVERDUE', 'LOST') NOT NULL DEFAULT 'BORROWED',
    renew_count INT NOT NULL DEFAULT 0,
    fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_borrow_reader FOREIGN KEY (reader_id) REFERENCES reader(id),
    CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES book(id),
    INDEX idx_borrow_time (borrow_time),
    INDEX idx_reader_id (reader_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User table for authentication
DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ADMIN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed readers
INSERT INTO reader (name, type, department, email, phone) VALUES
('Alice Zhang', 'STUDENT', 'Computer Science', 'alice@example.com', '13800000001'),
('Bob Li', 'STUDENT', 'Information Engineering', 'bob@example.com', '13800000002'),
('Cathy Wang', 'STUDENT', 'Mathematics', 'cathy@example.com', '13800000003'),
('David Chen', 'STUDENT', 'Physics', 'david@example.com', '13800000004'),
('Eva Liu', 'STUDENT', 'Economics', 'eva@example.com', '13800000005'),
('Frank Zhao', 'TEACHER', 'Computer Science', 'frank@example.com', '13800000006'),
('Grace Sun', 'TEACHER', 'History', 'grace@example.com', '13800000007'),
('Henry Guo', 'STAFF', 'Library', 'henry@example.com', '13800000008'),
('Ivy Xu', 'STAFF', 'Library', 'ivy@example.com', '13800000009'),
('Jackie Ma', 'TEACHER', 'Literature', 'jackie@example.com', '13800000010');

-- Seed users (password is plain text 'admin123' hashed by MD5 for demo only)
INSERT INTO user (username, password, role) VALUES
('admin', MD5('admin123'), 'ADMIN'),
('librarian', MD5('lib123'), 'STAFF'),
('viewer', MD5('viewer123'), 'VIEWER');

-- Seed books
INSERT INTO book (title, author, category, isbn, publish_year, total_copies) VALUES
('Spring Boot in Action', 'Craig Walls', 'Technology', '9781617292545', 2021, 8),
('Clean Code', 'Robert C. Martin', 'Technology', '9780132350884', 2020, 12),
('The Pragmatic Programmer', 'Andrew Hunt', 'Technology', '9780201616224', 2019, 10),
('Introduction to Algorithms', 'Cormen', 'Technology', '9780262046305', 2022, 6),
('Deep Learning', 'Ian Goodfellow', 'Technology', '9780262035613', 2021, 5),
('Sapiens', 'Yuval Noah Harari', 'History', '9780062316097', 2015, 15),
('Guns, Germs, and Steel', 'Jared Diamond', 'History', '9780393354324', 2018, 10),
('1984', 'George Orwell', 'Literature', '9780451524935', 2014, 20),
('To Kill a Mockingbird', 'Harper Lee', 'Literature', '9780061120084', 2010, 18),
('The Great Gatsby', 'F. Scott Fitzgerald', 'Literature', '9780743273565', 2013, 16),
('A Brief History of Time', 'Stephen Hawking', 'Science', '9780553380163', 2011, 14),
('The Selfish Gene', 'Richard Dawkins', 'Science', '9780198788607', 2016, 12),
('Astrophysics for People in a Hurry', 'Neil deGrasse Tyson', 'Science', '9780393609394', 2017, 10),
('The Art of War', 'Sun Tzu', 'History', '9781590302255', 2012, 10),
('The Story of Art', 'E. H. Gombrich', 'Art', '9780714832470', 2011, 8),
('Thinking, Fast and Slow', 'Daniel Kahneman', 'Business', '9780374533557', 2013, 14),
('Zero to One', 'Peter Thiel', 'Business', '9780804139298', 2014, 12),
('The Lean Startup', 'Eric Ries', 'Business', '9780307887894', 2016, 15),
('Design of Everyday Things', 'Don Norman', 'Design', '9780465050659', 2013, 10),
('The Psychology of Money', 'Morgan Housel', 'Business', '9780857197689', 2020, 18);

-- Seed borrow records (80 rows covering multiple months, categories, statuses)
INSERT INTO borrow_record (reader_id, book_id, borrow_time, due_time, return_time, status) VALUES
(1, 2, '2024-01-05 10:00:00', '2024-01-19 10:00:00', '2024-01-15 10:00:00', 'RETURNED'),
(1, 4, '2024-01-09 10:00:00', '2024-01-23 10:00:00', NULL, 'BORROWED'),
(1, 6, '2024-01-13 10:00:00', '2024-01-27 10:00:00', NULL, 'OVERDUE'),
(1, 8, '2024-01-17 10:00:00', '2024-01-31 10:00:00', '2024-01-27 10:00:00', 'RETURNED'),
(1, 10, '2024-01-21 10:00:00', '2024-02-04 10:00:00', '2024-01-31 10:00:00', 'RETURNED'),
(1, 12, '2024-01-25 10:00:00', '2024-02-08 10:00:00', NULL, 'BORROWED'),
(1, 14, '2024-01-29 10:00:00', '2024-02-12 10:00:00', NULL, 'OVERDUE'),
(1, 16, '2024-02-02 10:00:00', '2024-02-16 10:00:00', '2024-02-12 10:00:00', 'RETURNED'),
(2, 3, '2024-01-11 10:00:00', '2024-01-25 10:00:00', '2024-01-21 10:00:00', 'RETURNED'),
(2, 5, '2024-01-15 10:00:00', '2024-01-29 10:00:00', NULL, 'BORROWED'),
(2, 7, '2024-01-19 10:00:00', '2024-02-02 10:00:00', NULL, 'OVERDUE'),
(2, 9, '2024-01-23 10:00:00', '2024-02-06 10:00:00', '2024-02-02 10:00:00', 'RETURNED'),
(2, 11, '2024-01-27 10:00:00', '2024-02-10 10:00:00', '2024-02-06 10:00:00', 'RETURNED'),
(2, 13, '2024-01-31 10:00:00', '2024-02-14 10:00:00', NULL, 'BORROWED'),
(2, 15, '2024-02-04 10:00:00', '2024-02-18 10:00:00', NULL, 'OVERDUE'),
(2, 17, '2024-02-08 10:00:00', '2024-02-22 10:00:00', '2024-02-18 10:00:00', 'RETURNED'),
(3, 4, '2024-01-17 10:00:00', '2024-01-31 10:00:00', '2024-01-27 10:00:00', 'RETURNED'),
(3, 6, '2024-01-21 10:00:00', '2024-02-04 10:00:00', NULL, 'BORROWED'),
(3, 8, '2024-01-25 10:00:00', '2024-02-08 10:00:00', NULL, 'OVERDUE'),
(3, 10, '2024-01-29 10:00:00', '2024-02-12 10:00:00', '2024-02-08 10:00:00', 'RETURNED'),
(3, 12, '2024-02-02 10:00:00', '2024-02-16 10:00:00', '2024-02-12 10:00:00', 'RETURNED'),
(3, 14, '2024-02-06 10:00:00', '2024-02-20 10:00:00', NULL, 'BORROWED'),
(3, 16, '2024-02-10 10:00:00', '2024-02-24 10:00:00', NULL, 'OVERDUE'),
(3, 18, '2024-02-14 10:00:00', '2024-02-28 10:00:00', '2024-02-24 10:00:00', 'RETURNED'),
(4, 5, '2024-01-23 10:00:00', '2024-02-06 10:00:00', '2024-02-02 10:00:00', 'RETURNED'),
(4, 7, '2024-01-27 10:00:00', '2024-02-10 10:00:00', NULL, 'BORROWED'),
(4, 9, '2024-01-31 10:00:00', '2024-02-14 10:00:00', NULL, 'OVERDUE'),
(4, 11, '2024-02-04 10:00:00', '2024-02-18 10:00:00', '2024-02-14 10:00:00', 'RETURNED'),
(4, 13, '2024-02-08 10:00:00', '2024-02-22 10:00:00', '2024-02-18 10:00:00', 'RETURNED'),
(4, 15, '2024-02-12 10:00:00', '2024-02-26 10:00:00', NULL, 'BORROWED'),
(4, 17, '2024-02-16 10:00:00', '2024-03-01 10:00:00', NULL, 'OVERDUE'),
(4, 19, '2024-02-20 10:00:00', '2024-03-05 10:00:00', '2024-03-01 10:00:00', 'RETURNED'),
(5, 6, '2024-01-29 10:00:00', '2024-02-12 10:00:00', '2024-02-08 10:00:00', 'RETURNED'),
(5, 8, '2024-02-02 10:00:00', '2024-02-16 10:00:00', NULL, 'BORROWED'),
(5, 10, '2024-02-06 10:00:00', '2024-02-20 10:00:00', NULL, 'OVERDUE'),
(5, 12, '2024-02-10 10:00:00', '2024-02-24 10:00:00', '2024-02-20 10:00:00', 'RETURNED'),
(5, 14, '2024-02-14 10:00:00', '2024-02-28 10:00:00', '2024-02-24 10:00:00', 'RETURNED'),
(5, 16, '2024-02-18 10:00:00', '2024-03-03 10:00:00', NULL, 'BORROWED'),
(5, 18, '2024-02-22 10:00:00', '2024-03-07 10:00:00', NULL, 'OVERDUE'),
(5, 20, '2024-02-26 10:00:00', '2024-03-11 10:00:00', '2024-03-07 10:00:00', 'RETURNED'),
(6, 7, '2024-02-04 10:00:00', '2024-02-18 10:00:00', '2024-02-14 10:00:00', 'RETURNED'),
(6, 9, '2024-02-08 10:00:00', '2024-02-22 10:00:00', NULL, 'BORROWED'),
(6, 11, '2024-02-12 10:00:00', '2024-02-26 10:00:00', NULL, 'OVERDUE'),
(6, 13, '2024-02-16 10:00:00', '2024-03-01 10:00:00', '2024-02-26 10:00:00', 'RETURNED'),
(6, 15, '2024-02-20 10:00:00', '2024-03-05 10:00:00', '2024-03-01 10:00:00', 'RETURNED'),
(6, 17, '2024-02-24 10:00:00', '2024-03-09 10:00:00', NULL, 'BORROWED'),
(6, 19, '2024-02-28 10:00:00', '2024-03-13 10:00:00', NULL, 'OVERDUE'),
(6, 1, '2024-03-03 10:00:00', '2024-03-17 10:00:00', '2024-03-13 10:00:00', 'RETURNED'),
(7, 8, '2024-02-10 10:00:00', '2024-02-24 10:00:00', '2024-02-20 10:00:00', 'RETURNED'),
(7, 10, '2024-02-14 10:00:00', '2024-02-28 10:00:00', NULL, 'BORROWED'),
(7, 12, '2024-02-18 10:00:00', '2024-03-03 10:00:00', NULL, 'OVERDUE'),
(7, 14, '2024-02-22 10:00:00', '2024-03-07 10:00:00', '2024-03-03 10:00:00', 'RETURNED'),
(7, 16, '2024-02-26 10:00:00', '2024-03-11 10:00:00', '2024-03-07 10:00:00', 'RETURNED'),
(7, 18, '2024-03-01 10:00:00', '2024-03-15 10:00:00', NULL, 'BORROWED'),
(7, 20, '2024-03-05 10:00:00', '2024-03-19 10:00:00', NULL, 'OVERDUE'),
(7, 2, '2024-03-09 10:00:00', '2024-03-23 10:00:00', '2024-03-19 10:00:00', 'RETURNED'),
(8, 9, '2024-02-16 10:00:00', '2024-03-01 10:00:00', '2024-02-26 10:00:00', 'RETURNED'),
(8, 11, '2024-02-20 10:00:00', '2024-03-05 10:00:00', NULL, 'BORROWED'),
(8, 13, '2024-02-24 10:00:00', '2024-03-09 10:00:00', NULL, 'OVERDUE'),
(8, 15, '2024-02-28 10:00:00', '2024-03-13 10:00:00', '2024-03-09 10:00:00', 'RETURNED'),
(8, 17, '2024-03-03 10:00:00', '2024-03-17 10:00:00', '2024-03-13 10:00:00', 'RETURNED'),
(8, 19, '2024-03-07 10:00:00', '2024-03-21 10:00:00', NULL, 'BORROWED'),
(8, 1, '2024-03-11 10:00:00', '2024-03-25 10:00:00', NULL, 'OVERDUE'),
(8, 3, '2024-03-15 10:00:00', '2024-03-29 10:00:00', '2024-03-25 10:00:00', 'RETURNED'),
(9, 10, '2024-02-22 10:00:00', '2024-03-07 10:00:00', '2024-03-03 10:00:00', 'RETURNED'),
(9, 12, '2024-02-26 10:00:00', '2024-03-11 10:00:00', NULL, 'BORROWED'),
(9, 14, '2024-03-01 10:00:00', '2024-03-15 10:00:00', NULL, 'OVERDUE'),
(9, 16, '2024-03-05 10:00:00', '2024-03-19 10:00:00', '2024-03-15 10:00:00', 'RETURNED'),
(9, 18, '2024-03-09 10:00:00', '2024-03-23 10:00:00', '2024-03-19 10:00:00', 'RETURNED'),
(9, 20, '2024-03-13 10:00:00', '2024-03-27 10:00:00', NULL, 'BORROWED'),
(9, 2, '2024-03-17 10:00:00', '2024-03-31 10:00:00', NULL, 'OVERDUE'),
(9, 4, '2024-03-21 10:00:00', '2024-04-04 10:00:00', '2024-03-31 10:00:00', 'RETURNED'),
(10, 11, '2024-02-28 10:00:00', '2024-03-13 10:00:00', '2024-03-09 10:00:00', 'RETURNED'),
(10, 13, '2024-03-03 10:00:00', '2024-03-17 10:00:00', NULL, 'BORROWED'),
(10, 15, '2024-03-07 10:00:00', '2024-03-21 10:00:00', NULL, 'OVERDUE'),
(10, 17, '2024-03-11 10:00:00', '2024-03-25 10:00:00', '2024-03-21 10:00:00', 'RETURNED'),
(10, 19, '2024-03-15 10:00:00', '2024-03-29 10:00:00', '2024-03-25 10:00:00', 'RETURNED'),
(10, 1, '2024-03-19 10:00:00', '2024-04-02 10:00:00', NULL, 'BORROWED'),
(10, 3, '2024-03-23 10:00:00', '2024-04-06 10:00:00', NULL, 'OVERDUE'),
(10, 5, '2024-03-27 10:00:00', '2024-04-10 10:00:00', '2024-04-06 10:00:00', 'RETURNED');
