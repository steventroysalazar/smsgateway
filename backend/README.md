# SMS Sender Backend (Spring Boot + React Vite)

This folder now contains:
- a Spring Boot backend API (`/api/messages/...`) that talks to the Android SMS gateway
- a React + Vite frontend (`frontend/`) for sending SMS and manually fetching replies

## 1) Backend setup
Edit `src/main/resources/application.yml`:

- `gateway.base-url`: Android gateway URL (example `http://192.168.1.55:8082`)
- `gateway.token`: token shown in Android gateway app
- `gateway.default-limit`: max replies per fetch

Run backend:
```bash
cd backend
mvn spring-boot:run
```
Backend runs on `http://localhost:8090`.

## 2) Frontend setup (React + Vite)

Install and run:
```bash
cd backend/frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173` and proxies `/api` to `http://localhost:8090`.

## API exposed by backend

### Send SMS
`POST /api/messages/send`

```json
{
  "to": "+639xxxxxxxxx",
  "message": "Hello"
}
```

### Fetch replies
`GET /api/messages/replies?phone=+639xxxxxxxxx&since=1700000000000&limit=100`

Use `since` as your incremental cursor in milliseconds.


## Troubleshooting

- If Postman returns `connection timed out: connect`, your Spring backend cannot reach `gateway.base-url`.
- Verify the phone endpoint shown in the Android app matches `gateway.base-url` exactly (IP + port).
- Verify the Android gateway service is enabled.
- Verify `gateway.token` matches the Android app token exactly.
