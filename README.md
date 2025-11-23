# Safety API

A simple Java API for safety-related operations.

## Purpose
This API provides safety information based on your current location coordinates. It helps users assess the safety of their surroundings in Toronto.

## Data Source
- The scoring is based on Toronto assault reports collected over the last 2 years.
- The API analyzes recent incident data to generate a safety score for a given location.

## Features
- RESTful endpoints
- Basic safety checks
- Location-based safety scoring

## Technical Overview
- Built with Spring Boot (Java)
- RESTful API endpoints for health, scoring, and raw data
- Assault data is automatically downloaded and updated daily using a scheduled service
- Data is filtered to include only public area assaults from the last 2 years
- Safety scoring uses geospatial filtering and simple heuristics

## Setup
1. Clone the repository:
   ```bash
   git clone <repo-url>
   ```
2. Build the project using Maven:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Usage
Access the API at `http://localhost:8080` after starting the server.

### Endpoints
- `GET /api/score?lat={latitude}&lng={longitude}`
  - Returns a safety score and recent incident summary for the provided coordinates.
  - Example:
    ```bash
    curl "http://localhost:8080/api/score?lat=43.6532&lng=-79.3832"
    ```
- `GET /api/health`
  - Returns a simple health check response.
  - Example:
    ```bash
    curl "http://localhost:8080/api/health"
    ```
- `GET /api/raw/assault-data`
  - Returns the raw assault data used for scoring.
  - Example:
    ```bash
    curl "http://localhost:8080/api/raw/assault-data"
    ```