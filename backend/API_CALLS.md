# API Calls Reference

Base URL: `http://localhost:8090/api`

## Auth

### Register
`POST /auth/register`

```json
{
  "email": "manager@example.com",
  "password": "StrongPass123!",
  "firstName": "Mia",
  "lastName": "Manager",
  "contactNumber": "+639123456789",
  "address": "Main Office",
  "userRole": 2,
  "locationId": 1,
  "managerId": null
}
```

Roles:
- `1` = super admin
- `2` = manager
- `3` = user (must include `managerId`)

### Login
`POST /auth/login`
```json
{
  "email": "manager@example.com",
  "password": "StrongPass123!"
}
```

## Locations

### Create location
`POST /locations`
```json
{
  "name": "Manila HQ",
  "details": "Level 3, East Wing"
}
```

### List locations
`GET /locations`

Includes:
- location details
- users count under location
- devices count under location

## Users and Devices

### List users
`GET /users`

### List users under manager
`GET /users?managerId=2`

### Add device to user
`POST /users/{userId}/devices`
```json
{
  "name": "EV12 Wristband",
  "phoneNumber": "+639973079369"
}
```

### List user devices
`GET /users/{userId}/devices`

### List devices under location
`GET /locations/{locationId}/devices`

## Gateway messaging

### Send direct SMS
`POST /messages/send`
```json
{
  "to": "+639973079369",
  "message": "Hello"
}
```

### Fetch replies
`GET /messages/replies?phone=+639973079369&since=1700000000000&limit=100`

### Health check
`GET /messages/health`

### Debug effective config
`GET /messages/debug/config`

## EV12 config flow

### Send generated command set
`POST /send-config`

Use request body fields from your EV12 form. Backend will:
1. Build command segments.
2. Join with `;` preview.
3. Split to 150-char SMS chunks.
4. Send each chunk to the selected device number.

### Poll inbound messages
`GET /inbound-messages?phone=+639973079369&since=1700000000&limit=100`
