# Notenverwaltung (Grade Management System)

A Spring Boot REST application for managing student grades with JPA integration to a PostgreSQL database.

## Technologies Used

- **Spring Boot 3.2.0**: Core framework
- **Spring Data JPA**: Database access
- **PostgreSQL**: Database
- **Flyway**: Database migrations
- **Spring Security**: Authentication and authorization
- **OAuth2**: Google authentication
- **Docker**: Container for PostgreSQL
- **Lombok**: Reduce boilerplate code
- **JUnit & Spring Test**: Testing

## Project Structure

The project follows a standard layered architecture:

- **Controller**: REST endpoints
- **Service**: Business logic
- **Repository**: Data access
- **Model**: Entities and DTOs
- **Config**: Configuration classes

## Getting Started

### Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Google OAuth2 credentials

### Setup

1. Clone the repository
2. Set environment variables for Google OAuth2:
   ```
   GOOGLE_CLIENT_ID=your_client_id
   GOOGLE_CLIENT_SECRET=your_client_secret
   ```
3. Start the PostgreSQL database:
   ```
   docker-compose up -d
   ```
4. Run the application:
   ```
   ./gradlew bootRun
   ```

## API Endpoints

### Public Endpoints (No Authentication Required)

- `GET /api/public/info`: Get application information
- `GET /api/public/health`: Health check
- `GET /api/actuator/health`: Spring Boot Actuator health endpoint

### Protected Endpoints (Authentication Required)

#### User Management (Admin only)
- `GET /api/admin/users` (paged)
- `GET /api/admin/users/active` (paged)
- `POST /api/admin/users` (create user)
- `PUT /api/admin/users/{username}/password` (change password)
- `PUT /api/admin/users/{username}/active?active={true|false}` (activate/deactivate)
- `POST /api/admin/users/{username}/roles` (grant role)
- `DELETE /api/admin/users/{username}/roles/{role}` (revoke role)
- `DELETE /api/admin/users/{username}` (delete user)

#### Semesters
- `GET /api/semesters` (paged)
- `GET /api/semesters/{id}`
- `POST /api/semesters` (ADMIN)
- `PUT /api/semesters/{id}` (ADMIN)
- `DELETE /api/semesters/{id}` (ADMIN)

#### Subjects
- `GET /api/subjects` (paged)
- `GET /api/subjects/{id}`
- `POST /api/subjects` (ADMIN)
- `PUT /api/subjects/{id}` (ADMIN)
- `DELETE /api/subjects/{id}` (ADMIN)

#### Classes
- `GET /api/classes` (paged)
- `GET /api/classes/{id}`
- `POST /api/classes` (ADMIN)
- `PUT /api/classes/{id}` (ADMIN)
- `DELETE /api/classes/{id}` (ADMIN)

#### Tests
- `GET /api/tests` (paged)
- `GET /api/tests/{id}`
- `POST /api/tests` (ADMIN)
- `PUT /api/tests/{id}` (ADMIN)
- `DELETE /api/tests/{id}` (ADMIN)

#### Grades
- `GET /api/grades` (paged):
  - Admins: all grades; supports optional filters: studentId, testId, valueMin, valueMax
  - Users: only own grades
- `GET /api/grades/{id}`: Admin any; Users only their own grade
- `POST /api/grades` (ADMIN)
- `PUT /api/grades/{id}` (ADMIN)
- `DELETE /api/grades/{id}` (ADMIN)

## Authentication and Authorization

The application supports two authentication methods and implements role-based access control.

### OAuth2 Authentication

OAuth2 with Google is supported for authentication. This is useful for web applications that can redirect to Google's authentication page.

### JWT Token Authentication

The application also supports JWT token-based authentication, which is more suitable for API clients and mobile applications.

### Role-Based Access Control

The application implements role-based access control with two roles:

- ROLE_USER: General users (students) who can read domain data.
- ROLE_ADMIN: Administrators who can create, update, and delete domain data and manage users.

Key rules:
- Public endpoints under /api/public/** and /api/public/auth/** do not require a token.
- Read endpoints (GET) on /api/semesters, /api/subjects, /api/classes, /api/tests require a valid token with either ROLE_USER or ROLE_ADMIN.
- Write endpoints (POST, PUT, DELETE) on those resources require ROLE_ADMIN.
- All /api/admin/users/** endpoints require ROLE_ADMIN.

#### User Registration

To create a new user account, send a POST request to the registration endpoint:

```
POST /api/public/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123"
}
```

The response will contain a JWT token for the newly created user.

#### Default Admin Account

On first startup, if no user accounts exist in the database, a default admin account will be created automatically. The username and password for this account will be printed to the console. **It is strongly recommended to change this password immediately after first login.**

#### How to Obtain a JWT Token

To obtain a JWT token, send a POST request to the login endpoint:

```
POST /api/public/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "your-password"
}
```

The response will contain a JWT token:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

#### How to Use the JWT Token

To access protected endpoints, include the JWT token in the Authorization header of your requests:

```
GET /api/students
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Example Requests

Using curl:

```bash
# Obtain a JWT token
curl -X POST http://localhost:8080/api/public/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password"}'

# Access a protected endpoint
curl -X GET http://localhost:8080/api/students \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

All endpoints except those under `/api/public/**` and `/api/actuator/health` require authentication.

## Database

The application uses PostgreSQL as the database. The schema is managed by Flyway migrations.

### Entity-DTO Pattern

The application follows the Entity-DTO pattern:
- **Entities**: JPA entities that map directly to database tables
- **DTOs**: Data Transfer Objects used for API communication

This separation ensures that internal implementation details are not exposed through the API.

## Development Guidelines

1. Always create DTOs for API communication
2. Use proper validation for input data
3. Follow RESTful API design principles
4. Write unit tests for services and controllers
5. Document all public APIs
