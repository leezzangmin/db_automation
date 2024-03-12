drop database sample_schema;
create database sample_schema;
use sample_schema;

-- 고객 테이블
CREATE TABLE customers (
                           customer_id INT AUTO_INCREMENT PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           email VARCHAR(255) UNIQUE NOT NULL,
                           join_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 제품 테이블
CREATE TABLE products (
                          product_id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          price DECIMAL(10, 2) NOT NULL,
                          stock_quantity INT NOT NULL
);

-- 주문 테이블
CREATE TABLE orders (
                        order_id INT AUTO_INCREMENT PRIMARY KEY,
                        customer_id INT,
                        order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        status VARCHAR(50),
                        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- 고객 추가
DELIMITER //
CREATE PROCEDURE AddCustomer(IN p_name VARCHAR(255), IN p_email VARCHAR(255))
BEGIN
    INSERT INTO customers (name, email) VALUES (p_name, p_email);
END //
DELIMITER ;

-- 제품 추가
DELIMITER //
CREATE PROCEDURE AddProduct(IN p_name VARCHAR(255), IN p_price DECIMAL(10, 2), IN p_stock_quantity INT)
BEGIN
    INSERT INTO products (name, price, stock_quantity) VALUES (p_name, p_price, p_stock_quantity);
END //
DELIMITER ;

-- 주문 추가
DELIMITER //
CREATE PROCEDURE AddOrder(IN p_customer_id INT)
BEGIN
    INSERT INTO orders (customer_id, status) VALUES (p_customer_id, 'Processing');
END //
DELIMITER ;


-- 고객별 총 주문 수
CREATE VIEW CustomerOrderCount AS
SELECT customer_id, COUNT(*) AS total_orders
FROM orders
GROUP BY customer_id;

-- 제품별 재고 상태
CREATE VIEW ProductStockStatus AS
SELECT product_id, name, CASE WHEN stock_quantity > 0 THEN 'In Stock' ELSE 'Out of Stock' END AS stock_status
FROM products;

-- 최근 주문
CREATE VIEW RecentOrders AS
SELECT order_id, customer_id, order_date
FROM orders
ORDER BY order_date DESC
LIMIT 10;


-- 주문 시 재고 감소
DELIMITER //
CREATE TRIGGER AfterOrderInsert AFTER INSERT ON orders
    FOR EACH ROW
BEGIN
    UPDATE products SET stock_quantity = stock_quantity - 1 WHERE product_id = 1;
END //
DELIMITER ;

-- 고객 추가 시 환영 이메일 발송 (시뮬레이션)
DELIMITER //
CREATE TRIGGER WelcomeEmailAfterCustomerInsert AFTER INSERT ON customers
    FOR EACH ROW
BEGIN
    -- 여기서는 실제 이메일 발송 코드를 구현할 수 없으므로, 예시로만 제공합니다.
    UPDATE products SET stock_quantity = stock_quantity - 2 WHERE product_id = 2;
END //
DELIMITER ;

-- 제품 재고 변경 시 로깅
DELIMITER //
CREATE TRIGGER LogStockChange AFTER UPDATE ON products
    FOR EACH ROW
BEGIN
    UPDATE products SET stock_quantity = stock_quantity - 3 WHERE product_id = 3;
END //
DELIMITER ;



-- 제품 가격에 세금 추가 계산
DELIMITER //
CREATE FUNCTION f1(p_price DECIMAL(10,2))
    RETURNS DECIMAL(10,2)
BEGIN
    RETURN p_price * 1.11;
END //
DELIMITER ;

DELIMITER //
CREATE FUNCTION f2(p_price DECIMAL(10,2))
    RETURNS DECIMAL(10,2)
BEGIN
    RETURN p_price * 1.12;
END //
DELIMITER ;

DELIMITER //
CREATE FUNCTION f3(p_price DECIMAL(10,2))
    RETURNS DECIMAL(10,2)
BEGIN
    RETURN p_price * 1.13;
END //
DELIMITER ;

