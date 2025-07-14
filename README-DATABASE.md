# PostgreSQL Database Creation Issue

## Issue
The application was failing to start with the error:
```
FATAL: no existe la base de datos 'voting_db'
```
This means the PostgreSQL database 'voting_db' doesn't exist in the container.

## Solution
We've implemented a comprehensive approach to ensure the database is created and the application can connect to it:

1. Created an initialization script (`init.sql`) that explicitly creates the database using direct SQL commands.
2. Removed the `POSTGRES_DB` environment variable from the Docker configuration to avoid potential conflicts.
3. Added HikariCP connection pool settings to the application configuration to handle connection retries and delays.

This approach ensures that the database is properly created when the container starts and that the application can connect to it even if there are delays in the database initialization.

## Steps to Apply the Changes

1. **Create the initialization script** (if it doesn't exist):
   Create a file named `init.sql` in the project root with the following content:
   ```sql
   -- Create the voting_db database if it doesn't exist
   -- This script is executed by the postgres user when the container starts

   -- Simple approach: just try to create the database
   -- If it already exists, the command will fail but the script will continue
   CREATE DATABASE voting_db;

   -- Grant privileges to the postgres user
   ALTER DATABASE voting_db OWNER TO postgres;
   ```

2. **Stop the current PostgreSQL container**:
   ```bash
   docker stop esp-voting-postgres
   ```

3. **Remove the container** (this will delete all data in the database):
   ```bash
   docker rm esp-voting-postgres
   ```

4. **Start the container again with the new configuration**:
   ```bash
   docker-compose up -d
   ```
   or
   ```bash
   docker compose up -d
   ```

5. **Verify the container is running**:
   ```bash
   docker ps
   ```

6. **Check the logs to ensure the container started successfully**:
   ```bash
   docker logs esp-voting-postgres
   ```
   You should see messages indicating that PostgreSQL has started successfully and that the initialization script was executed.

7. **Update the application.yaml file** to add HikariCP connection pool settings:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/voting_db
       username: postgres
       password: postgres
       driver-class-name: org.postgresql.Driver
       hikari:
         connection-timeout: 20000
         maximum-pool-size: 5
         minimum-idle: 2
         initialization-fail-timeout: 30000
   ```

8. **Start the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

## Troubleshooting
If you're still experiencing issues, try the following:

1. **Connect to the PostgreSQL container**:
   ```bash
   docker exec -it esp-voting-postgres bash
   ```

2. **Connect to the PostgreSQL server**:
   ```bash
   psql -U postgres
   ```

3. **List all databases**:
   ```sql
   \l
   ```
   Check if 'voting_db' is in the list.

4. **If 'voting_db' is not in the list, create it manually**:
   ```sql
   CREATE DATABASE voting_db;
   ```

5. **Exit the PostgreSQL shell and container**:
   ```bash
   \q
   exit
   ```

6. **Start the application again**:
   ```bash
   ./mvnw spring-boot:run
   ```

## Extending the Solution
We're already using an initialization script (`init.sql`) to create the database. If you need to perform additional initialization tasks, you can modify this script or add more scripts:

1. To add more commands to the existing script, edit `init.sql`:
   ```sql
   -- Create the voting_db database if it doesn't exist
   -- This script is executed by the postgres user when the container starts

   -- Simple approach: just try to create the database
   -- If it already exists, the command will fail but the script will continue
   CREATE DATABASE voting_db;

   -- Grant privileges to the postgres user
   ALTER DATABASE voting_db OWNER TO postgres;

   -- Add additional commands here
   -- For example, to create another database:
   -- CREATE DATABASE additional_db;
   -- ALTER DATABASE additional_db OWNER TO postgres;

   -- Or to create tables, users, etc.
   ```

2. Alternatively, you can add more scripts to the `/docker-entrypoint-initdb.d/` directory by updating the `compose.yaml` file:
   ```yaml
   volumes:
     - postgres_data:/var/lib/postgresql/data
     - ./init.sql:/docker-entrypoint-initdb.d/init.sql
     - ./additional-script.sql:/docker-entrypoint-initdb.d/additional-script.sql
   ```

The PostgreSQL Docker image automatically executes any .sql, .sql.gz, or .sh files found in the /docker-entrypoint-initdb.d/ directory in alphabetical order when the container is first started.

## Understanding the HikariCP Connection Pool Settings

We've added HikariCP connection pool settings to the application.yaml file to help with database connection issues:

- **connection-timeout**: 20000 ms (20 seconds) - How long to wait for a connection from the pool before timing out. This gives the database more time to respond when it's under load or still initializing.

- **maximum-pool-size**: 5 - Maximum number of connections in the pool. This limits the number of concurrent connections to the database, which can help prevent overloading it.

- **minimum-idle**: 2 - Minimum number of idle connections in the pool. This ensures that there are always some connections available for immediate use.

- **initialization-fail-timeout**: 30000 ms (30 seconds) - How long to wait for the pool to initialize before failing. This is particularly important for our issue, as it gives the database more time to initialize before the application gives up trying to connect.

These settings help the application handle the case where it tries to connect to the database before it's fully initialized. The application will retry connecting to the database for up to 30 seconds before giving up, which should be enough time for the database to initialize in most cases.
