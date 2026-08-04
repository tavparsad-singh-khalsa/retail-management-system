#!/bin/sh
set -eu

for db in retail_management sales_db Customer_db retail_billing retail_notification retail_report; do
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres -c "CREATE DATABASE \"$db\";"
done
