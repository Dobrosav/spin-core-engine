# Spin Core Engine

Spin Core Engine is a Spring Boot application designed to handle the core logic for a slot game. It exposes REST APIs
for game interaction, manages player data, and stores game state using a MySQL database.

## Prerequisites

Before you begin, ensure you have the following installed on your machine:

* **Java 21**: Required for building and running the application locally.
* **Maven**: Dependency management and build tool.
* **Docker & Docker Compose**: Recommended for easy setup and running the application along with its database.

## Environment Setup

The application requires an environment file (`.env`) in the root directory to provide the database password. A default
`.env` file is already included in the repository.

Example `.env` content:

```env
DB_PASSWORD=slot_password
```

## Running the Application

### Option 1: Using Docker Compose (Recommended)

This is the easiest way to get the application running. Docker Compose will start both the MySQL database and the Spring
Boot application, linking them automatically.

1. Make sure Docker is running.
2. Open a terminal in the project root directory.
3. Run the following command to build and start the containers in the background:
   ```bash
   docker-compose up -d --build
   ```
4. The application will be accessible at `http://localhost:11150`.
5. To stop the application, run:
   ```bash
   docker-compose down
   ```

### Option 2: Running Locally (Manual Setup)

If you prefer to run the application outside of Docker, you'll need to set up the MySQL database manually.

1. **Start a MySQL Database**: You can use the `docker-compose.yml` just to start the database:
   ```bash
   docker-compose up -d db
   ```
   *Alternatively, ensure you have a local MySQL server running with the following details:*
    * Host: `localhost`
    * Port: `3306`
    * Database Name: `slot_game`
    * Username: `slot_user`
    * Password: `slot_password` (or whatever is in your `.env` file)

2. **Build the application**:
   ```bash
   mvn clean install -DskipTests
   ```

3. **Run the application**:
   Ensure the `DB_PASSWORD` environment variable is available in your shell, or pass it directly.
   ```bash
   DB_PASSWORD=slot_password mvn spring-boot:run
   ```
   Or run the generated JAR:
   ```bash
   export DB_PASSWORD=slot_password
   java -jar target/spin-core-engine-0.0.1.jar
   ```

## API Documentation

The application uses Springdoc OpenAPI (Swagger) to document its REST APIs. Once the application is running, you can
access the Swagger UI to view and interact with the endpoints.

* **Swagger UI URL**: [http://localhost:11150/swagger-ui/index.html](http://localhost:11150/swagger-ui/index.html)
* **OpenAPI JSON**: [http://localhost:11150/v3/api-docs](http://localhost:11150/v3/api-docs)

## Running Tests

The project uses JUnit 5 and Testcontainers to spin up a real MySQL database during the test phase for integration
testing.

To run all tests, ensure Docker is running (required by Testcontainers) and execute:

```bash
mvn test
```
