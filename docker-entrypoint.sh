#!/bin/bash

echo "Waiting for config-service..."
/wait-for-it.sh 192.168.192.2:8888 --timeout=60 --strict -- echo "config-service is up"

echo "Waiting for discovery-service..."
/wait-for-it.sh 192.168.192.2:8761 --timeout=60 --strict -- echo "discovery-service is up"

echo "Waiting for mariadb-auth..."
/wait-for-it.sh 192.168.192.2:3307 --timeout=60 --strict -- echo "mariadb-auth is up"

echo "Waiting for redis-token..."
/wait-for-it.sh 192.168.192.2:6379 --timeout=60 --strict -- echo "redis-token to is up"

echo "Starting auth-service"
exec java -jar app.jar