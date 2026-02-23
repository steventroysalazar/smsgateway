# SMS Sender Backend (Spring Boot + React Vite)

This backend now includes:
- Gateway bridge APIs (`/api/messages/...`) to send/fetch SMS via Android gateway
- SQL-backed user/device management (`/api/users...`)
- Device configuration APIs (`/api/send-config`, `/api/inbound-messages`) for EV12-style command flow
- React + Vite frontend (`frontend/`)

## 1) Backend setup
Edit `src/main/resources/application.yml`:

- `gateway.base-url`: Android gateway URL (example `http://192.168.1.37:8082`)
- `gateway.token`: token shown in Android gateway app
- `gateway.default-limit`: max replies per fetch

Database (SQL) is enabled using H2 file storage:
- DB file: `backend/data/smsgateway.mv.db`
- H2 console: `http://localhost:8090/h2-console`

Run backend:
```bash
cd backend
mvn spring-boot:run
```

## 2) Frontend setup (React + Vite)
```bash
cd backend/frontend
npm install
npm run dev
```

## API (new user/device flow)

### Create user
`POST /api/users`
```json
{
  "name": "John Doe",
  "email": "john@example.com"
}
```

### List users
`GET /api/users`

### Add device to user
`POST /api/users/{userId}/devices`
```json
{
  "name": "EV12 Wristband",
  "phoneNumber": "+639973079369"
}
```

### List user devices
`GET /api/users/{userId}/devices`

## API (EV12 config flow)

### Send generated EV12 command SMS
`POST /api/send-config`

```json
{
  "deviceId": 1,
  "contactNumber": "+639111111111",
  "smsPassword": "123456",
  "requestLocation": true,
  "wifiEnabled": true,
  "checkBattery": true,
  "workingMode": "mode1",
  "checkStatus": true
}
```

The backend builds the semicolon-separated command preview, splits into 150-char SMS chunks, and sends each chunk to the device number via Android gateway.

### Fetch inbound messages for UI polling
`GET /api/inbound-messages?phone=+639973079369&since=1700000000&limit=100`

- `since` supports seconds or milliseconds epoch values
- Response is formatted for frontend usage with fields:
  - `id`
  - `from`
  - `text`
  - `receivedAt` (ISO timestamp)

## Existing gateway bridge API

- `POST /api/messages/send`
- `GET /api/messages/replies`
- `GET /api/messages/health`
- `GET /api/messages/debug/config`

## Runtime override headers

- `X-Gateway-Base-Url`: override target gateway URL
- `Authorization`: raw API key (no `Bearer` prefix)

## Troubleshooting

- If direct phone call works but backend fails, call `GET /api/messages/debug/config` and verify resolved base URL/token.
- If backend returns `Gateway send failed: HTTP 500, body: Error: 500`, the Android gateway rejected the request. Check SIM, SMS permission/default app status, token, and phone number format.
