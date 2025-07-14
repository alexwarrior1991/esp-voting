-- Create the voting_db database if it doesn't exist
-- This script is executed by the postgres user when the container starts

-- Check if the database exists before trying to create it
DO
$$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'voting_db') THEN
    CREATE DATABASE voting_db;
END IF;
END
$$;

-- Asegurarse de que el usuario postgres existe y tiene los permisos adecuados
DO
$$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'postgres') THEN
CREATE ROLE postgres WITH LOGIN SUPERUSER PASSWORD 'postgres';
END IF;
END
$$;

-- Grant privileges to the postgres user
ALTER DATABASE voting_db OWNER TO postgres;
