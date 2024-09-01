#!/bin/bash

docker exec -i redis redis-cli FLUSHALL

docker exec -i db mysql mysql -uroot -proot -e "
USE quick_deal;
TRUNCATE TABLE payment;
TRUNCATE TABLE order_product;
DELETE FROM \`order\`;
ALTER TABLE \`order\` AUTO_INCREMENT = 1;
"