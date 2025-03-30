#!/bin/bash

# Wait for the database to start
sleep 60

# Start the listener
lsnrctl start

#wait for the listener to start
sleep 10

# Check if the listener is running
lsnrctl status

# Create a new user and grant privileges
sqlplus system/123@localhost:1521/XE as sysdba <<EOF
@/docker-entrypoint-initdb.d/init.sql
EXIT;
EOF
# CREATE USER ruianUser IDENTIFIED BY "12345";
# GRANT CONNECT, RESOURCE TO ruianUser;
# EXIT;
# EOF

# # Execute the init.sql script as the new user
# sqlplus ruianUser/12345@localhost:1521/XE <<EOF
# @/docker-entrypoint-initdb.d/init.sql
# EXIT;
# EOF