#!/bin/bash

# kafka
MAX_PARTITIONS=10
# Delete the Kafka topic
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --delete --topic quick-deal.order.creation.request-5 --bootstrap-server localhost:9092

# Recreate the Kafka topic with the specified number of partitions
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --create --topic quick-deal.order.creation.request-5 --partitions $MAX_PARTITIONS --replication-factor 1 --bootstrap-server localhost:9092

# redis
docker exec -i redis redis-cli FLUSHALL

# mysql
docker exec -i mysql mysql mysql -uroot -proot -e "
CREATE DATABASE IF NOT EXISTS quick_deal;
USE quick_deal;
TRUNCATE TABLE payment;
TRUNCATE TABLE order_product;
DELETE FROM \`order\`;
ALTER TABLE \`order\` AUTO_INCREMENT = 1;
"