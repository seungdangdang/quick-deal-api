CREATE TABLE `user`
(
    id         BIGINT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    grade      VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `category`
(
    id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE `product`
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id    BIGINT,
    name           VARCHAR(255) NOT NULL,
    description    TEXT         NOT NULL,
    price          INT          NOT NULL,
    stock_quantity INT          NOT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE `order`
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT   NOT NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    process_status ENUM('PROCESS', 'DONE', 'ERROR'),
    FOREIGN KEY (user_id) REFERENCES `user` (id)
);

CREATE TABLE `order_product`
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id   BIGINT,
    product_id BIGINT,
    quantity   INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order` (id),
    FOREIGN KEY (product_id) REFERENCES `product` (id)
);

CREATE TABLE `payment`
(
    order_id       BIGINT PRIMARY KEY,
    payment_amount int,
    payment_date   DATETIME,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    process_status ENUM('PROCESS', 'DONE', 'ERROR'),
    FOREIGN KEY (order_id) REFERENCES `order` (id)
);
