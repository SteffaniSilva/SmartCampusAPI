# Smart Campus Sensor & Room Management API

**Student:** Steffani Silva  
**Student ID:** w2120418  
**Module:** 5COSC022C.2 Client-Server Architectures  

---

## 📖 Overview

The Smart Campus API is a RESTful web service developed using **JAX-RS** and **Jersey**. It is designed to support the university’s Smart Campus initiative by allowing facilities managers and automated systems to manage physical locations (**Rooms**), hardware devices (**Sensors**), and historical telemetry (**Sensor Readings**).

### Key Architectural Features

- **Resource-Oriented Design:** A clear resource hierarchy based on real campus entities such as rooms and sensors.
- **Versioned API Entry Point:** All endpoints are exposed under `/api/v1`.
- **In-Memory Data Store:** Uses Java collections instead of a database, as required by the coursework.
- **Sub-Resource Routing:** Supports nested endpoints such as `/sensors/{id}/readings`.
- **Advanced Error Handling:** Custom exceptions are mapped to meaningful HTTP responses like `409`, `422`, `403`, and `500`.
- **Global Logging:** Request and response logging is handled using JAX-RS filters.

### Main Resources

- `GET /api/v1`
- `GET /api/v1/rooms`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms/{roomId}`
- `DELETE /api/v1/rooms/{roomId}`
- `GET /api/v1/sensors`
- `POST /api/v1/sensors`
- `GET /api/v1/sensors/{sensorId}`
- `GET /api/v1/sensors/{sensorId}/readings`
- `POST /api/v1/sensors/{sensorId}/readings`

---

## 🛠 Technologies Used

- Java
- JAX-RS
- Jersey
- Apache Tomcat
- Maven
- Apache NetBeans
- Postman

---

## 🚀 Build and Launch Instructions

### Prerequisites

- JDK 17 or higher
- Apache Maven
- Apache Tomcat
- Apache NetBeans (recommended)

### Option 1: Run in NetBeans

1. Open the project in **Apache NetBeans**.
2. Make sure **Apache Tomcat** is selected as the server.
3. Right-click the project and choose **Clean and Build**.
4. Right-click the project and choose **Run**.
5. Open the API discovery endpoint in the browser or Postman:

```text
http://localhost:8080/SmartCampusApi/api/v1
```

### Option 2: Build with Maven and deploy manually

1. Open a terminal in the project folder.
2. Run:

```bash
mvn clean install
```

3. This will generate a `.war` file in the `target/` directory.
4. Copy `SmartCampusApi.war` into the `webapps/` folder of Apache Tomcat.
5. Start Tomcat.
6. Open:

```text
http://localhost:8080/SmartCampusApi/api/v1
```

> If the deployed context path is different on another machine, replace `SmartCampusApi` with the actual deployed application name.

---

## 📡 Sample cURL Commands

### 1. View API discovery endpoint

```bash
curl -i http://localhost:8080/SmartCampusApi/api/v1
```

### 2. Get all rooms

```bash
curl -i http://localhost:8080/SmartCampusApi/api/v1/rooms
```

### 3. Create a new room

```bash
curl -i -X POST http://localhost:8080/SmartCampusApi/api/v1/rooms \
-H "Content-Type: application/json" \
-d "{\"id\":\"ENG-101\",\"name\":\"Engineering Lab\",\"capacity\":40}"
```

### 4. Get a room by ID

```bash
curl -i http://localhost:8080/SmartCampusApi/api/v1/rooms/ENG-101
```

### 5. Register a valid sensor

```bash
curl -i -X POST http://localhost:8080/SmartCampusApi/api/v1/sensors \
-H "Content-Type: application/json" \
-d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":400.0,\"roomId\":\"ENG-101\"}"
```

### 6. Register an invalid sensor with a missing room reference

```bash
curl -i -X POST http://localhost:8080/SmartCampusApi/api/v1/sensors \
-H "Content-Type: application/json" \
-d "{\"id\":\"CO2-999\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":410.0,\"roomId\":\"NO-ROOM\"}"
```

### 7. Filter sensors by type

```bash
curl -i "http://localhost:8080/SmartCampusApi/api/v1/sensors?type=CO2"
```

### 8. Add a new reading to a sensor

```bash
curl -i -X POST http://localhost:8080/SmartCampusApi/api/v1/sensors/CO2-001/readings \
-H "Content-Type: application/json" \
-d "{\"value\":421.7}"
```

### 9. Get reading history for a sensor

```bash
curl -i http://localhost:8080/SmartCampusApi/api/v1/sensors/CO2-001/readings
```

### 10. Trigger a room deletion conflict

```bash
curl -i -X DELETE http://localhost:8080/SmartCampusApi/api/v1/rooms/ENG-101
```

### 11. Trigger the global 500 error mapper

```bash
curl -i http://localhost:8080/SmartCampusApi/api/v1/debug/crash
```

---

# 📝 Conceptual Report Answers

## Chapter 1: Setup & Discovery

### 1. 1.	Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

• By default, JAX-RS resource classes follow a request-scoped lifecycle. This means the JAX-RS runtime (such as Jersey) instantiates a completely new instance of the resource class for every incoming HTTP request and discards it once the response is sent.

  While the alternative lifecycle is the @Singleton pattern—where one single instance serves all requests—the default request-scoped behavior has significant architectural implications. Because the class is re-instantiated, any instance-level variables are lost between requests. Consequently, to persist data in this project, I implemented a separate static DataStore.

  Because this static store is shared across multiple concurrent request threads, I utilized ConcurrentHashMap to ensure thread safety. This prevents race conditions and data corruption that could occur if two requests attempted to modify the same collection (such as adding a Room or a Sensor) simultaneously. By using these high-performance concurrent collections, the API remains "leak-proof" and maintains data integrity without the need for a traditional database.

### 2. Why is the provision of Hypermedia (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

• Hypermedia is considered a hallmark of advanced RESTful design because the server includes navigational links in its responses, allowing clients to discover available actions dynamically instead of relying entirely on hardcoded URLs or external documentation. This reduces coupling between client and server, makes the API more self-descriptive, and improves resilience if endpoint structures change over time. Compared with static documentation alone, hypermedia makes the API easier to explore and evolve because clients can follow links provided by the server rather than assuming fixed routes. In this project, the discovery endpoint demonstrates this idea in a simplified form by returning links for rooms, sensors, and sensor readings.

---

## Chapter 2: Room Management

### 3. When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

• Returning only room IDs reduces payload size, saves bandwidth, and lowers serialization overhead on the server, which can be useful for very large collections. However, it pushes more work onto the client because the client must make additional requests to fetch the details for each room, increasing complexity and total network round trips. Returning full room objects increases payload size, but it is more convenient for clients because the required data is available immediately in one response. In this coursework, returning full room objects is more appropriate because the dataset is relatively small and it keeps the API simpler and easier to use.

### 4. Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

• Yes, the DELETE operation is idempotent. If a room exists and has no assigned sensors, the first DELETE request removes it successfully. If the exact same DELETE request is sent again, the room no longer exists, so the server returns a 404 Not Found. Although the response codes differ between the first and later requests, the final state of the system is the same after every repeated request: the room does not exist. Because repeated identical requests do not create any additional side effects after the first successful deletion, the operation satisfies the definition of idempotency.

---

## Chapter 3: Sensors & Filtering

### 5. We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as `text/plain` or `application/xml`. How does JAX-RS handle this mismatch?

• The @Consumes(MediaType.APPLICATION_JSON) annotation tells JAX-RS that the resource method only accepts request bodies in JSON format. If a client sends a different media type, such as text/plain or application/xml, the request does not match the method’s declared input type. In that situation, JAX-RS typically rejects the request before it reaches the resource method and returns HTTP 415 Unsupported Media Type. This behavior enforces a clear API contract, prevents incompatible payloads from being processed, and avoids internal parsing errors caused by unsupported formats.

### 6. You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g. `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?

• Query parameters are generally superior for filtering and searching because they represent optional constraints on an existing collection rather than defining a completely different resource. The endpoint /api/v1/sensors still refers to the same sensor collection, while ?type=CO2 simply narrows the result set. This approach is more flexible, easier to extend with additional filters such as ?type=CO2&status=ACTIVE, and aligns with common REST conventions for filtering, sorting, and pagination. In contrast, embedding filters inside the path makes the API more rigid and can lead to unnecessary endpoint variations for what is really just a search condition.

---

## Chapter 4: Sub-Resources

### 7. Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., `sensors/{id}/readings/{rid}`) in one massive controller class?

• The Sub-Resource Locator pattern improves modularity, separation of concerns, and maintainability by delegating nested functionality to dedicated classes. Instead of placing all sensor and reading logic inside one large controller, the main SensorResource can act as a router and delegate /sensors/{id}/readings requests to a separate SensorReadingResource. This keeps each class focused on a smaller responsibility, makes the code easier to read and test, and prevents the creation of a large monolithic controller. In larger APIs, this pattern helps control complexity and allows nested resources to evolve independently while still preserving a clear URI hierarchy. 

---

## Chapter 5: Error Handling & Logging

### 8. Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

• HTTP 422 Unprocessable Entity is often more semantically accurate because the request itself is syntactically valid, the endpoint exists, and the JSON can be parsed correctly, but the server cannot process the request because one of the values inside it is logically invalid. In this coursework, posting a new sensor with a roomId that does not exist is not the same as requesting a missing URL resource, so returning 404 Not Found would be less precise. Using 422 clearly communicates that the payload format was valid but the referenced dependency inside the payload could not be satisfied.

### 9. From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

• Exposing internal Java stack traces is a cybersecurity risk because it leaks sensitive information about the application’s internal structure. An attacker could learn package names, class names, method names, file paths, framework details, library versions, database-related logic, and even the exact line of code where the failure occurred. This information can help an attacker map the architecture of the system, identify weak points, and craft more targeted exploits against known vulnerabilities. Returning sanitized JSON error responses instead of raw stack traces reduces information leakage and improves the overall security posture of the API.

### 10. Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?

• Using JAX-RS filters for logging is advantageous because logging is a cross-cutting concern that should apply consistently across many endpoints. A request/response filter allows the logging logic to be implemented once and automatically applied to every incoming request and outgoing response. This avoids duplication, keeps resource methods focused on business logic, and makes the system easier to maintain. If Logger.info() statements were inserted manually into every resource method, the code would become repetitive, harder to keep consistent, and easier to forget when adding new endpoints. Centralizing logging in filters is therefore cleaner, more scalable, and better aligned with separation of concerns. 

---

## ✅ Video Demonstration Checklist

The video demonstrates:

- `GET /api/v1` discovery endpoint
- `GET /rooms`
- `POST /rooms`
- `GET /rooms/{id}`
- valid `POST /sensors`
- invalid `POST /sensors` with missing `roomId` dependency
- sensor filtering with `?type=...`
- `POST /sensors/{id}/readings`
- `GET /sensors/{id}/readings`
- `403 Forbidden` for a maintenance sensor
- `409 Conflict` for deleting a room with linked sensors
- clean `500 Internal Server Error` with no stack trace exposed

---

## 🔗 GitHub Repository

Public GitHub repository link:

```text
https://github.com/SteffaniSilva/SmartCampusAPI.git
```

---

## Notes

- This project only uses **JAX-RS**, as required by the coursework.
- No database was used.
- All data is stored in memory using Java collections.
- The API was tested using Postman.