# Distributed File Storage System with Kafka and Zookeeper

This project implements a distributed file storage system where files are split into chunks and stored in Kafka, while Zookeeper is used to manage file metadata and track the status of files. It supports uploading, downloading, and verifying file metadata, and is designed to handle both small and large files efficiently.

Additionally, the project includes a simple frontend example and provides unit and integration tests for validating the backend functionality.

## Features

- **Kafka-based File Storage**: Large files are chunked and stored in Kafka.
- **Zookeeper Metadata Management**: Zookeeper handles file metadata and tracks file statuses.
- **Dynamic Partition Load Balancing**: Kafka partitions are dynamically assigned based on the total size of files being stored, optimizing load balancing.
- **Frontend Example**: A simple frontend to interact with the file storage system.
- **Unit and Integration Tests**: Ensure the correctness of the backend services.

## Project Structure

The project is organized into the following modules:

- **Backend**: The core logic of the file storage system with Kafka and Zookeeper integration.
- **Frontend Example**: A simple example of a frontend to interact with the file storage system.
- **Docker Compose**: Two Docker Compose configurations to help run the application in a containerized environment.
    - `docker-compose.yml` (compose-app): Starts all the services (Kafka, Zookeeper, backend).
    - `docker-compose-tools.yml` (compose-tools): Starts only the tools for local development (Kafka, Zookeeper, etc.) without the full application stack.

## Prerequisites

Before running the project, ensure that you have the following installed:

- Docker and Docker Compose
- Java 21 or higher
- Gradle
- Kotlin 1.8 or higher

## Running the Project

### Using Docker Compose

There are two `docker-compose` configurations in this project:

1. **`docker-compose.yml` (compose-app)**: This configuration starts all necessary services, including Kafka, Zookeeper, and the backend.

   To start the entire application stack, run:
   ```bash
   docker-compose -f compose-app.yml up
   ```

2. **`docker-compose-tools.yml` (compose-tools)**: This configuration starts only the tools (Kafka, Zookeeper) for local development or testing without the full application.

   To start only the tools, run:
   ```bash
   docker-compose -f compose-tools.yml up
   ```

### Running Tests

To run tests, you can execute the following Gradle commands:

- **Unit Tests**: Run unit tests with the following command:
  ```bash
  ./gradlew unitTest
  ```

- **Integration Tests**: Run integration tests with the following command:
  ```bash
  ./gradlew integrationTest
  ```

### Frontend Example

There is an example frontend in the `frontend-example` directory that demonstrates how to interact with the backend service. This can be used to upload and download files, as well as view file metadata.

To run the frontend example:

1. Navigate to the `frontend-example` directory.
2. Set up a simple HTTP server or integrate it into your preferred development environment.