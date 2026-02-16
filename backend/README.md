# SMS Sender Backend (Spring Boot)

A minimal backend + UI that works with the Android gateway implementation in this repo.

## Features
- Send SMS through Android gateway (`POST /api/messages/send`)
- Manually fetch replies for last sent number (`GET /api/messages/replies`)
- Incremental fetch support using `since` timestamp cursor
- Simple static frontend served by Spring Boot (`/`)

## Configure
Edit `src/main/resources/application.yml`:

- `gateway.base-url`: Android gateway URL (example `http://192.168.1.55:8082`)
- `gateway.token`: token shown in Android gateway screen
- `gateway.default-limit`: max replies per fetch

## Run
```bash
mvn spring-boot:run
```

Then open:
- `http://localhost:8090`

## API
### Send
`POST /api/messages/send`

```json
{
  "to": "+639xxxxxxxxx",
  "message": "Hello"
}
```

### Fetch replies
`GET /api/messages/replies?phone=+639xxxxxxxxx&since=1700000000000&limit=100`

`since` is in milliseconds and should be persisted by your backend/web app for incremental polling.
