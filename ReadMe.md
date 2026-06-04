# Pulse Polling

Pulse Polling is a full-stack survey platform that allows users to create, share, take, and manage surveys through a modern web interface.

## Features

### Authentication & User Management

- User registration with password validation requirements
- Secure login using JWT
- BCrypt password hashing
- Protected routes for authenticated users
- User-specific survey ownership
- Persistent login state on the frontend

### Survey Creation

- Create custom surveys
- Add multiple survey questions
- Configure answer options
- Store surveys in PostgreSQL
- Associate surveys with their creator

### Survey Management

- User state Dashboard only displays surveys owned by the authenticated user
- Survey visibility controls:
  - PRIVATE
  - UNLISTED
  - PUBLIC
- Soft delete support
- Shareable survey links
- Survey ownership enforcement

### Survey Sharing

- Public survey sharing via unique share tokens
- Social media sharing support
- Open Graph metadata for rich link previews
- Facebook, LinkedIn, and X integration

### Frontend

- React + Vite
- React Router
- Protected routes
- Responsive dashboard interface

### Backend

- Spring Boot
- Spring Security
- JWT Authentication
- JPA/Hibernate
- PostgreSQL

## Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Security
- PostgreSQL
- JWT
- BCrypt

### Frontend
- React
- Vite
- JavaScript
- CSS

### Infrastructure
- Docker
- Docker Compose
- Render
- Vercel

## Local Development

### Start PostgreSQL

```bash
docker compose up -d
```

### Start Backend

```bash
gradlew bootRun
```

### Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

Backend URL:

```text
http://localhost:8080
```

## Future Enhancements

- Survey analytics dashboard
- Statistical analysis and visualizations
- Public survey discovery
- Export survey results

## Author

Jasyn Bruemmer

LinkedIn: https://www.linkedin.com/in/jasyn-leo-bruemmer

GitHub: https://github.com/jasynbruemmer


