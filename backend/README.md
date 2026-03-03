# SMS Sender Backend (Spring Boot + React Vite)

## What is included
- Android SMS gateway bridge endpoints
- User registration/login (role-based: super admin, manager, user)
- SQL-backed users, devices, locations
- EV12 command generation and SMS send flow
- React + Vite portal frontend

## Run backend
```bash
cd backend
mvn spring-boot:run
```

Backend: `http://localhost:8090`

### Quick env setup (recommended)
Copy the template and fill in your real values:

```bash
cd backend
cp .env.example .env
```

Then load it before running Spring Boot:

```bash
set -a
source .env
set +a
mvn spring-boot:run
```

## Run frontend
```bash
cd backend/frontend
npm install
npm run dev
```

Frontend: `http://localhost:5173`

---

## SQL database setup (how to connect)

### Default local SQL (already configured)
This project uses H2 file DB by default:
- URL: `jdbc:h2:file:./data/smsgateway;MODE=PostgreSQL;AUTO_SERVER=TRUE`
- Console: `http://localhost:8090/h2-console`

Config is in `src/main/resources/application.yml`.

### Connect to PostgreSQL (recommended for production)
PostgreSQL JDBC dependency is already included in `pom.xml`.

Set these environment variables before starting backend:

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/smsgateway?sslmode=require'
export SPRING_DATASOURCE_DRIVER_CLASS_NAME='org.postgresql.Driver'
export SPRING_DATASOURCE_USERNAME='your_user'
export SPRING_DATASOURCE_PASSWORD='your_password'
export SPRING_H2_CONSOLE_ENABLED='false'
export WEBHOOK_EV12_TOKEN='replace_me'
```

Then run:

```bash
cd backend
mvn spring-boot:run
```

Because this project now reads datasource config from env vars, it can run both:
- local H2 (default, no env vars)
- cloud PostgreSQL (set env vars)

### Neon example (from Neon "Connect" panel)
If Neon gives you a URL like:

`postgresql://neondb_owner:PASSWORD@ep-xxxx.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require`

convert it to Spring JDBC URL:

`jdbc:postgresql://ep-xxxx.ap-southeast-1.aws.neon.tech/neondb?sslmode=require`

Then set:

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://ep-xxxx.ap-southeast-1.aws.neon.tech/neondb?sslmode=require'
export SPRING_DATASOURCE_USERNAME='neondb_owner'
export SPRING_DATASOURCE_PASSWORD='your_neon_password'
export SPRING_DATASOURCE_DRIVER_CLASS_NAME='org.postgresql.Driver'
export SPRING_H2_CONSOLE_ENABLED='false'
export WEBHOOK_EV12_TOKEN='replace_me'
```

### Connect to MySQL (alternative)
1. Add MySQL driver:
```xml
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
  <scope>runtime</scope>
</dependency>
```
2. Use datasource config:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smsgateway?useSSL=false&allowPublicKeyRetrieval=true
    username: your_user
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
```

---

## API docs
See full endpoint examples in:
- `backend/API_CALLS.md`

UI/UX flow reference for product and design:
- `backend/UI_FLOW_PLAN.md`

### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`

### Locations
- `POST /api/locations`
- `GET /api/locations`

### Users/devices
- `GET /api/users`
- `GET /api/users?managerId={id}`
- `POST /api/users/{userId}/devices`
- `GET /api/users/{userId}/devices`
- `GET /api/locations/{locationId}/devices`

### Messaging
- `POST /api/messages/send`
- `GET /api/messages/replies`
- `GET /api/messages/health`
- `GET /api/messages/debug/config`

### EV12 flow
- `POST /api/send-config`
- `GET /api/inbound-messages`
- `POST /api/webhooks/ev12`
- `GET /api/webhooks/ev12/events`

## Roles
- `1` super admin
- `2` manager
- `3` user

Rules implemented:
- Role 3 users must be assigned to a role 2 manager.
- Manager can have users assigned via `managerId`.
- Locations return user/device counts.

### EV12 webhook (board -> backend)
Devices can push raw board telemetry/config snapshots directly to backend for monitoring first:

- Endpoint: `POST /api/webhooks/ev12`
- Header: `X-Webhook-Token: <WEBHOOK_EV12_TOKEN>` (optional unless token configured)
- Body: raw JSON payload from EV12 board.

You can inspect recent ingested events using:

- `GET /api/webhooks/ev12/events?limit=20`

