# Smart Campus API

A JAX-RS coursework project for **5COSC022W Client-Server Architectures**.

---

## 1. Project Overview

This project implements a RESTful Smart Campus API for managing:

* Rooms
* Sensors
* Sensor Readings

The system allows users to:

* manage rooms and sensors
* track environmental data
* enforce business rules using structured error handling

Technologies used:

* JAX-RS (Jersey)
* Grizzly HTTP Server
* Java in-memory collections (`ConcurrentHashMap`, `ArrayList`)

---

## 2. REST API Design

The API follows REST principles by exposing **resources (nouns)** and interacting via **HTTP methods (verbs)**.

### Core resources

* Room
* Sensor
* SensorReading

### Main endpoints

* GET `/api/v1`
* GET `/api/v1/rooms`
* POST `/api/v1/rooms`
* GET `/api/v1/rooms/{roomId}`
* DELETE `/api/v1/rooms/{roomId}`
* GET `/api/v1/sensors`
* POST `/api/v1/sensors`
* GET `/api/v1/sensors/{sensorId}`
* GET `/api/v1/sensors/{sensorId}/readings`
* POST `/api/v1/sensors/{sensorId}/readings`

---

## 3. Architecture & Design Decisions

### JAX-RS lifecycle

Resource classes are request-scoped, meaning a new instance is created per request.
To maintain shared data, the system uses a static `DataStore`.

---

### Data storage

The API uses:

* `ConcurrentHashMap` for rooms and sensors
* thread-safe lists for readings

This prevents race conditions and supports concurrent access.

---

### Resource hierarchy

Sensor readings are modeled as a sub-resource:

```
/sensors/{sensorId}/readings
```

---

### Separation of concerns

The project separates:

* resources (API endpoints)
* models (data)
* exception mappers (errors)
* filters (logging)
* datastore (shared state)

---

## 4. Discovery Endpoint

```
GET /api/v1
```

Returns:

```json
{
  "apiVersion": "v1",
  "adminContact": "admin@smartcampus.ac.uk",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors",
    "sensorReadingsTemplate": "/api/v1/sensors/{sensorId}/readings"
  }
}
```

### Why useful

Provides API navigation and reduces hardcoded endpoints.
This reflects a simplified HATEOAS approach.

---

## 5. Data Models

### Room

```java
public class Room {
    private String id;
    private String name;
    private int capacity;
    private List<String> sensorIds;
}
```

### Sensor

```java
public class Sensor {
    private String id;
    private String type;
    private String status;
    private double currentValue;
    private String roomId;
}
```

### SensorReading

```java
public class SensorReading {
    private String id;
    private long timestamp;
    private double value;
}
```

---

## 6. Error Handling

The API uses ExceptionMapper classes to return structured JSON errors.

### Error codes

* 409 → room not empty
* 422 → invalid room reference
* 403 → sensor unavailable
* 404 → not found
* 500 → unexpected error

### Example

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room contains sensors and cannot be deleted"
}
```

---

## 7. Logging

A filter logs:

* HTTP method
* request URI
* response status

---

## 8. Build & Run

### Requirements

* Java 17+
* Maven

### Run

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="org.example.Main"
```

API URL:

```
http://localhost:8080/api/v1
```

---

## 9. Sample curl Commands

### Discovery

```bash
curl -i http://localhost:8080/api/v1
```

---

### Create Room

```bash
curl -i -X POST http://localhost:8080/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{
"id":"ENG-201",
"name":"Engineering Room",
"capacity":50,
"sensorIds":[]
}'
```

---

### Get Rooms

```bash
curl -i http://localhost:8080/api/v1/rooms
```

---

### Get Room by ID

```bash
curl -i http://localhost:8080/api/v1/rooms/ENG-201
```

---

### Create Sensor

```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{
"id":"TEMP-900",
"type":"Temperature",
"status":"ACTIVE",
"currentValue":22.3,
"roomId":"ENG-201"
}'
```

---

### Filter Sensors

```bash
curl -i "http://localhost:8080/api/v1/sensors?type=Temperature"
```

---

### Add Reading

```bash
curl -i -X POST http://localhost:8080/api/v1/sensors/TEMP-900/readings \
-H "Content-Type: application/json" \
-d '{
"id":"READ-001",
"timestamp":1710000000000,
"value":23.8
}'
```

---

### Get Readings

```bash
curl -i http://localhost:8080/api/v1/sensors/TEMP-900/readings
```

---

### Error Test (Invalid Room)

```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{
"id":"TEMP-999",
"type":"Temperature",
"status":"ACTIVE",
"currentValue":20.0,
"roomId":"INVALID"
}'
```

---

### Error Test (Delete Room with Sensors)

```bash
curl -i -X DELETE http://localhost:8080/api/v1/rooms/ENG-201
```

---

## 10. Testing Summary

Tested:

* endpoint functionality
* resource creation
* filtering
* sub-resources
* error handling

---

## 11. Limitations

* in-memory storage (data resets)
* no authentication
* no database

---

## 12. Conclusion

This project demonstrates a RESTful API using JAX-RS, applying resource-based design, proper HTTP methods, structured error handling, and sub-resource architecture.


## Demo Video
