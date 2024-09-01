CREATE TABLE `product`
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_type  VARCHAR(255) NOT NULL,
    name           VARCHAR(255) NOT NULL,
    description    TEXT         NOT NULL,
    price          INT          NOT NULL,
    stock_quantity INT          NOT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `order`
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_uuid      VARCHAR(255) NOT NULL,
    process_status ENUM('PROCESSING', 'DONE', 'CANCEL', 'ERROR'),
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `order_product`
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT    NOT NULL,
    price      INT    NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order` (id),
    FOREIGN KEY (product_id) REFERENCES `product` (id)
);

CREATE TABLE `payment`
(
    order_id       BIGINT PRIMARY KEY,
    payment_amount int,
    payment_date   DATETIME,
    process_status ENUM('PROCESSING', 'DONE', 'CANCEL', 'ERROR'),
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES `order` (id)
);
