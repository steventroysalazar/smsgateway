# [Traccar SMS Gateway for Android](https://www.traccar.org/sms-gateway/)

[![Get it on Google Play](http://www.tananaev.com/badges/google-play.svg)](https://play.google.com/store/apps/details?id=org.traccar.gateway)

## Overview

Traccar SMS Gateway is an Android messaging app. The key difference from other messaging apps is an option to expose a local-network HTTP API for sending and receiving SMS messages through the phone.

The project is based on another open open source project - [Simple SMS Messenger](https://github.com/SimpleMobileTools/Simple-SMS-Messenger).

## Team

- Anton Tananaev ([anton@traccar.org](mailto:anton@traccar.org))

## License

    GNU General Public License, Version 3

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.


## API

The gateway API is local-network only (served from your phone on port 8082).

### Send SMS

```http
POST /
Authorization: <token>
Content-Type: application/json

{
  "to": "+10000000000",
  "message": "Your message",
  "slot": 0
}
```

### Read incoming replies

```http
GET /messages?phone=+10000000000&since=1700000000000&limit=100
Authorization: <token>
```

The `GET /messages` endpoint returns SMS messages received by the phone (inbox) so your web/API client can display replies for numbers you messaged through the gateway. Use optional query parameters:

- `phone`: filter by sender phone number (supports normalized matching for number formats like `+63...` vs `09...`).
- `since`: unix timestamp in milliseconds; only return newer messages.
- `limit`: max number of returned messages (default 100, max 1000).
