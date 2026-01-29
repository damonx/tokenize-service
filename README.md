# Tokenization Service (Spring Boot)

## Project Objective

The purpose of this project is to implement a **simple, secure, and high‑performance tokenization service** using Spring Boot.

Tokenization is a common pattern in the financial domain used to protect sensitive data such as payment card numbers or bank account numbers. Instead of storing or transmitting the original sensitive value, the system replaces it with an opaque token that has **no intrinsic meaning** outside of the service.

The solution is intentionally **not production‑ready** and omits concerns such as authentication, authorization, encryption at rest, and auditing.

---

## Architecture Overview

The application follows a standard Spring Boot layered architecture:

```
Client
  │
  ▼
REST Controller
  │
  ▼
TokenizationService (business logic)
  │
  ▼
JPA Repository
  │
  ▼
H2 In‑Memory Database
```

### Key Components

* **TokenizationController**
  Exposes REST endpoints for tokenization and detokenization.

* **TokenizationService**
  Encapsulates all tokenization and detokenization logic.

* **TokenRepository (JPA)**
  Persists token ↔ account number mappings in an in‑memory H2 database.

* **GlobalExceptionHandler**
  Provides consistent error responses for REST APIs.

* **Spring Cache**
  Improves detokenization performance by avoiding repeated database lookups.

---

## API Endpoints

### Tokenize

**POST** `/tokenize`

**Request Body**

```json
[
  "4111-1111-1111-1111",
  "4444-3333-2222-1111"
]
```

**Response**

```json
[
  "fvMymE7X0Je1IzMDgWooV5iGBPw0yoFy",
  "L4hKuBJHxe67ENSKLVbdIH8NhFefPui2"
]
```

---

### Detokenize

**POST** `/detokenize`

**Request Body**

```json
[
  "fvMymE7X0Je1IzMDgWooV5iGBPw0yoFy"
]
```

**Response**

```json
[
  "4111-1111-1111-1111"
]
```


---

## Performance & Caching Design

* Tokenization is **idempotent**: the same account number will always return the same token.
* Detokenization is a **read‑heavy operation** in most real‑world systems.
* A Spring Cache is applied to the `detokenize` operation to:

    * Reduce database lookups
    * Improve response latency
    * Demonstrate awareness of performance optimization

Caching is intentionally scoped to detokenization only, keeping token creation logic simple and deterministic.

---

## Validation & Error Handling

* Input validation is implemented using **Hibernate Validator**.
* Invalid or empty requests are rejected early.
* A global `@RestControllerAdvice` ensures consistent error responses.

---

## Running the Application

### Prerequisites

* Java 21

### Run Locally

```bash
chmod +x gradlew
./gradlew bootRun
```

The application will start on:

```bash
http://localhost:8080
```

### Smoke Tests
* Case A: Tokenization
```bash
curl -Sv -X POST localhost:8080/tokenize -H "Content-Type: application/json" -d '["4111-1111-1111-1111","4444-3333-2222-1111"]'
Note: Unnecessary use of -X or --request, POST is already inferred.
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> POST /tokenize HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.7.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 45
> 
* upload completely sent off: 45 bytes
< HTTP/1.1 200 
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Wed, 28 Jan 2026 08:01:49 GMT
< 
* Connection #0 to host localhost left intact
["uS8vN3dph7ttuKMHbuk4Hsbbln1aAvLY","KOWVIS6eevmE48loZ7IzKCKppkXuP3xx"]
```

* Case B: De-tokenization
```bash
curl -Sv localhost:8080/detokenize -H "Content-Type: application/json" -d '["uS8vN3dph7ttuKMHbuk4Hsbbln1aAvLY","KOWVIS6eevmE48loZ7IzKCKppkXuP3xx"]'
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> POST /detokenize HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.7.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 36
> 
* upload completely sent off: 36 bytes
< HTTP/1.1 200 
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Wed, 28 Jan 2026 08:18:00 GMT
< 
* Connection #0 to host localhost left intact
["4111-1111-1111-1111", "4444-3333-2222-1111"]
```

* Case C: token NOT FOUND error
```bash
curl -Sv localhost:8080/detokenize -H "Content-Type: application/json" -d '["GZt5XL5gmFl1gxBziER7cPQZ2tfvHvaa"]'
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> POST /detokenize HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.7.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 36
> 
* upload completely sent off: 36 bytes
< HTTP/1.1 404 
< Content-Type: application/problem+json
< Transfer-Encoding: chunked
< Date: Wed, 28 Jan 2026 12:21:21 GMT
< 
* Connection #0 to host localhost left intact
{"detail":"Token not found: GZt5XL5gmFl1gxBziER7cPQZ2tfvHvaa","instance":"/detokenize","status":404,"title":"Token Not Found","Timestamp":"2026-01-28T12:21:21.883540Z"}
```

* Case D: wrong token format BAD_REQUEST
```bash
curl -Sv localhost:8080/detokenize -H "Content-Type: application/json" -d '["3vDEvKAPxej5rorMMGFbXH3AILQe6Baakdkjdfkdf"]'
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> POST /detokenize HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.7.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 45
> 
* upload completely sent off: 45 bytes
< HTTP/1.1 400 
< Content-Type: application/problem+json
< Transfer-Encoding: chunked
< Date: Wed, 28 Jan 2026 13:08:37 GMT
< Connection: close
< 
* Closing connection
{"detail":"Request validation failed","instance":"/detokenize","status":400,"title":"Validation Error","errors":["Wrong token format."],"timestamp":"2026-01-28T13:08:37.851997Z"}
```

---

## Running Tests

```bash
./gradlew test
```
---
Here’s a **README-ready version**, reorganised into **Short-term / Medium-term / Long-term** phases.
The language is concise, professional, and suitable for technical reviewers or architects.

---

## Improvement Roadmap for `tokenization-service`

The current implementation focuses on core functionality and clarity.
The following roadmap outlines recommended enhancements, grouped by delivery horizon.

---

## Short-term Improvements (Immediate / Low Effort)

These changes improve API quality, maintainability, and developer experience with minimal architectural impact.

### API Design & Consistency

* **Adopt OpenAPI 3.0 (Swagger) as the API contract**

    * Define request/response schemas, error models, and examples.
    * Generate server-side DTOs and client SDKs from the contract.
    * Enable frontend and downstream teams to rely on a single source of truth.

* **Introduce explicit Request and Response DTOs**

    * Avoid using `List<String>` directly in controller methods.
    * Enables inclusion of metadata such as requestId, validation results, and warnings.

* **Standardise API responses**

    * Use a consistent response structure with:

        * `status` / `code`
        * `httpStatus`
        * `message`
        * `data`
        * `errors`
    * Align error handling with **RFC 7807 (Problem Details)**.

### Validation & Error Handling

* Improve validation error responses with clear, actionable messages.
* Ensure all client errors return appropriate HTTP 4xx responses.

### Documentation

* Enhance Swagger documentation with example requests and responses.
* Expand README with local development and testing instructions.

---

## Medium-term Improvements (Scalability & Reliability)

These changes prepare the service for real-world usage, higher traffic, and operational robustness.

### Security & Access Control

* **Add authentication and authorisation**

    * Secure endpoints using OAuth2 / OpenID Connect (e.g. JWT).
    * Apply role- or scope-based access control.

* **Enable auditing and traceability**

    * Log security-sensitive actions (tokenization / detokenization).
    * Include correlation IDs for request tracing.

### Resilience & Performance

* **Introduce rate limiting**

    * Protect APIs from abuse and accidental overload.
    * Implement via API Gateway, Resilience4j, or reverse proxy.

* **Apply resilience patterns**

    * Timeouts, retries, and circuit breakers.
    * Fail fast when downstream dependencies are unavailable.

### Testing & Quality

* **Add integration tests using Testcontainers**

    * Spin up real dependencies (e.g. PostgreSQL, Redis).
    * Validate behavior in a production-like environment.

* **Introduce baseline and regression performance tests**

    * Ensure new changes do not degrade throughput or latency.

---

## Long-term Improvements (Enterprise & Production-grade)

These changes support enterprise-scale deployment, compliance, and observability.

### Scalability & Architecture

* **Use Redis for shared state and caching**

    * Enable horizontal scalability with multiple pod replicas.
    * Support stateless application instances.

* **Introduce API versioning**

    * e.g. `/v1/tokenize`
    * Allow safe evolution without breaking consumers.

### Observability & Operations

* **Structured logging**

    * Emit logs in JSON format.
    * Include traceId, requestId, and userId where applicable.

* **Metrics and monitoring**

    * Expose metrics using Micrometer and Prometheus.
    * Monitor latency, error rates, and throughput.

* **Distributed tracing**

    * Integrate OpenTelemetry for end-to-end request tracing.

### Compliance & Governance

* **Data protection and compliance**

    * Ensure sensitive data is never logged in plaintext.
    * Encrypt data at rest and in transit.
    * Align with PCI-DSS or equivalent regulatory standards where applicable.

* **Contract and compatibility testing**

    * Introduce consumer-driven contract tests.
    * Guarantee backward compatibility across releases.

