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
1. Add PostgreSQL driver to `pom.xml`:
```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```
2. Update `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smsgateway
    username: your_user
    password: your_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
```
3. Restart backend.

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

## Roles
- `1` super admin
- `2` manager
- `3` user

Rules implemented:
- Role 3 users must be assigned to a role 2 manager.
- Manager can have users assigned via `managerId`.
- Locations return user/device counts.
