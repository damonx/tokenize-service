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

```
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
```base
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