# UI/UX Flow Plan — Device Configuration Portal

This document is aligned with the current backend implementation in this repository (`backend/`), including:
- role-based registration/login
- user-manager hierarchy
- location grouping
- devices owned by users
- command-based SMS configuration flow

---

## 1) Product Goal

Create a portal where teams can:
1. Register and log in users by role.
2. Organize users/devices by location.
3. Assign role-3 users under role-2 managers.
4. Register devices per user.
5. Build and send SMS commands to devices.
6. Read inbound replies and show command outcomes.

---

## 2) Roles and Access Model

### Role definitions
- **Role 1 — Super Admin**
  - Full access across locations, users, and devices.
  - Can create managers and users.
- **Role 2 — Manager**
  - Can view/manage users assigned to this manager.
  - Can register devices for users under this manager.
- **Role 3 — User**
  - End-user/device owner profile.
  - Must be assigned to a manager (role 2).

### Backend rules already enforced
- Role 3 users require `managerId` on registration.
- Assigned manager must be role 2.
- Users can optionally be assigned to a location.

---

## 3) Core Information Architecture

### Main modules
1. **Auth**
2. **Dashboard**
3. **Users**
4. **Locations**
5. **Devices**
6. **Device Settings / Command Builder**
7. **Inbound Replies**

### Suggested left navigation
- Dashboard
- Users
- Locations
- Devices
- Commands
- Replies
- Profile / Logout

---

## 4) Screen-by-Screen UX Flow

## 4.1 Authentication

### A. Register
Fields:
- Email
- Password
- First Name
- Last Name
- Contact Number
- Address
- Role (1/2/3)
- Location (optional)
- Manager (required if role=3)

Primary action:
- **Create account**

Validations:
- Required fields present.
- Email unique.
- If role=3, manager must be selected and role=2.

### B. Login
Fields:
- Email
- Password

Primary action:
- **Sign in**

On success:
- Route to role-based dashboard.

---

## 4.2 Dashboard (Post-login)

Show cards with quick metrics:
- Total users (scope depends on role)
- Total devices
- Total locations
- Recent inbound replies

Device overview card (selected device):
- Device Name
- Device Phone Number
- Owner User
- Owner Location
- Last reply timestamp
- Last known battery text (if present in replies)

Quick actions:
- Send Command
- Request Location (`loc`)
- Fetch Replies

---

## 4.3 Users Module

### List users
Columns:
- Name
- Email
- Role
- Contact
- Location
- Manager
- Device Count

Role-specific behavior:
- Super Admin: sees all users.
- Manager: sees only users assigned to self.

### Create user modal/page
Same fields as register flow.

Important UX note:
- If role is changed to **User (3)**, automatically reveal and require manager selector.

---

## 4.4 Locations Module

### List locations
Columns:
- Location Name
- Details
- Users Count
- Devices Count

### Create location
Fields:
- Name
- Details

### Location detail page (recommended)
Tabs:
- Users under this location
- Devices under this location

---

## 4.5 Devices Module

### List devices
Columns:
- Device Name
- Phone Number
- Owner
- Owner Role
- Location

Actions:
- Open Device
- Configure Device
- Fetch Replies

### Add device
Fields:
- Device Name
- Phone Number
- Owner User

Rule:
- For manager view, owner dropdown should include only users under that manager.

---

## 4.6 Device Settings / Command Builder

Goal: Build one semicolon-separated SMS payload, preview it, then send.

### Sections (recommended)
1. Core setup
   - Contact Number (`A1,1,1,<number>`)
   - SMS Password (`P<password>`)
   - Request location toggle (`loc`)

2. Connectivity
   - Wi-Fi (`Wifi0/1`)
   - APN (`S0/1,<apn>`)
   - Server (`IP0/1,<host>,<port>`)
   - GPRS (`S0/S2`)

3. Audio & identity
   - Mic volume (`Micvolume`)
   - Speaker volume (`Speakervolume`)
   - Prefix (`prefix0/1,<name>`)

4. Alarms
   - Battery check (`battery`)
   - Fall detection (`fl0/1,sensitivity,call`)
   - No motion (`nmo0/1,time,call`)

5. Working mode
   - mode1..mode6 with conditional interval fields

6. Continuous locate
   - `CL<interval>,<duration>`

7. Diagnostics
   - Status (`status`)

### UX requirements
- Real-time **command preview**.
- Character count + note that payload will be split by backend into 150-char SMS chunks.
- Final send confirmation with device name + phone number.

---

## 4.7 Replies / Inbound Messages

### Replies page
Filters:
- Device
- Phone
- Since timestamp
- Limit

List fields:
- Received At
- From
- Message Text

Actions:
- Manual refresh
- Start/stop polling (every few seconds)

Behavior:
- Keep `since` cursor to load only new replies.

---

## 5) Primary User Journeys

### Journey A — Super Admin setup
1. Create locations.
2. Register managers.
3. Register users and assign manager/location.
4. Add devices to users.
5. Validate replies from test commands.

### Journey B — Manager operations
1. Login as manager.
2. See only assigned users.
3. Register/add devices to assigned users.
4. Configure device via command builder.
5. Monitor inbound replies.

### Journey C — User support scenario
1. User profile opened.
2. Device selected.
3. Quick commands sent (`loc`, `status`, etc.).
4. Support operator verifies reply and updates notes.

---

## 6) Command Library for UI Designer

## Most common commands (pin these in UI)
- Contact: `A1,1,1,<phone>`
- Password: `P<password>`
- Location now: `loc`
- Fall alarm: `fl1,5,1`
- No motion: `nmo1,80M,1`
- Geo/continuous tracking (if supported): `CL10s,600s`
- Status: `status`

## Advanced/optional command catalog
- Prefix: `prefix1,Emma`
- Wi-Fi: `Wifi1`
- APN: `S1,<apn>`
- Server: `IP1,<host>,<port>`
- GPRS: `S2`
- Mode: `mode1`, `mode2,<a>,<b>`, etc.
- Speaker: `Speakervolume90`

> Note: Keep advanced commands inside an expandable “Advanced” section to reduce cognitive load.

---

## 7) API Mapping for UI Implementation

Use these backend routes (see also `API_CALLS.md`):

### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`

### Users/devices
- `GET /api/users`
- `GET /api/users?managerId={id}`
- `POST /api/users/{userId}/devices`
- `GET /api/users/{userId}/devices`

### Locations
- `POST /api/locations`
- `GET /api/locations`
- `GET /api/locations/{locationId}/devices`

### Messaging
- `POST /api/messages/send`
- `GET /api/messages/replies`
- `POST /api/send-config`
- `GET /api/inbound-messages`

---

## 8) UX Quality Checklist

- Clear role visibility (badge: Super Admin / Manager / User).
- Manager-scoped views must not leak unrelated users/devices.
- Validation messages should be actionable and near input fields.
- Command preview must be copyable.
- Replies list should support long messages and timestamps.
- Mobile responsive layout for all forms.

---

## 9) Suggested Next UX Enhancements

1. Add a visual command template picker (Common / Advanced).
2. Add saved presets per device type.
3. Add timeline view for outbound command and inbound reply pairs.
4. Add explicit permission matrix page for role capabilities.

