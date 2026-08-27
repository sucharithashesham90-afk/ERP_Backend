-- Run this as PostgreSQL superuser (postgres)
-- Script: Create ERP Platform database and application user

-- Create dedicated database user
CREATE USER erp_user WITH PASSWORD 'erp@2024secure';

-- Create database
CREATE DATABASE erp_platform
    WITH OWNER = erp_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE erp_platform TO erp_user;

-- Connect to erp_platform and grant schema privileges
\c erp_platform
GRANT ALL ON SCHEMA public TO erp_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO erp_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO erp_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO erp_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO erp_user;
