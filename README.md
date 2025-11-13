# Pulse Stream Gateway

Pulse Stream Gateway is a lightweight Spring Boot microservice that simulates Hookline's ingest edge. It accepts synthetic signal payloads, normalizes them, and emits structured events for downstream services. The project mirrors the class layout used by LogTracer so the demo logs map cleanly back to real source files.

## Features
- `/signals/ingest` endpoint that walks through handshake, normalization, and dispatch stages
- Structured SLF4J logging aligned with the Pulse demo datasets
- Actuator health endpoint for basic observability
- Ready-to-fork Maven project targeting Java 17 and Spring Boot 3

## Getting Started
```bash
sdk use java 17-tem           # or any JDK 17+
mvn spring-boot:run
```
The service listens on `http://localhost:8081` by default. Send a sample request:
```bash
curl -X POST http://localhost:8081/signals/ingest \
     -H 'Content-Type: application/json' \
     -d '{"assetId":"buoy-0441","codec":"delta","sessionId":"relay-17822"}'
```

## Project Layout
```
src/main/java/io/hookline/pulse/ingest/StreamGatewayApplication.java
src/main/java/io/hookline/pulse/ingest/controller/StreamGateway.java
src/main/java/io/hookline/pulse/ingest/model/SignalRequest.java
```

Feel free to expand the API surface or wire the controller into a real message bus—the logging contract is already in place for Hookline's GitHub integration demos.
