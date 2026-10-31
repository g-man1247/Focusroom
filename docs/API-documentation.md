# Focusroom V2 — API & Endpoint Documentation

## 1. Overview
Focusroom V2 provides server-rendered MVC views via Thymeleaf, complemented by REST JSON endpoints for the Pomodoro timer engine and WebSocket STOMP messaging for real-time room communication.

---

## 2. Authentication & Account Management

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/login` | Public | Displays sign-in view. Supports query params `?error`, `?logout`, `?expired`. |
| `POST`| `/login` | Public | Spring Security authentication entrypoint (form login). |
| `GET` | `/register` | Public | Displays account creation form. |
| `POST`| `/register` | Public | Validates and registers a new student account (`UserRegistrationDto`). |
| `POST`| `/logout` | Authenticated | Terminates user session, clears cookies, and redirects to `/login`. |
| `GET` | `/profile` | Authenticated | Displays student profile and credentials form. |
| `POST`| `/profile/update` | Authenticated | Updates student full name, email, and bio (`UserProfileDto`). |
| `POST`| `/profile/change-password` | Authenticated | Verifies current password and updates hash with BCrypt. |

---

## 3. Dashboard & Productivity

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/dashboard` | Authenticated | Personalized dashboard containing study stats, streak, active rooms, and weekly chart. |
| `GET` | `/analytics` | Authenticated | Full analytics dashboard with 14-day trend, subject breakdown, and session history table. |
| `GET` | `/notifications` | Authenticated | Lists all persistent alerts and milestone achievements for the user. |
| `POST`| `/notifications/{id}/read` | Authenticated | Marks a single notification as read. |
| `POST`| `/notifications/read-all` | Authenticated | Marks all unread notifications for the user as read. |

---

## 4. Virtual Study Rooms

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/rooms` | Authenticated | Browses active public rooms; supports query param `?search=...`. |
| `GET` | `/rooms/create` | Authenticated | Displays study room creation form. |
| `POST`| `/rooms/create` | Authenticated | Creates a new study room and assigns the creator as room OWNER. |
| `GET` | `/rooms/{id}` | Authenticated | View dedicated room workspace (timer, chat, participants). Prompts for access code if private. |
| `POST`| `/rooms/{id}/join` | Authenticated | Submits access code to verify and join private rooms. |
| `POST`| `/rooms/{id}/leave` | Authenticated | Sets student room membership to inactive. |
| `POST`| `/rooms/{id}/archive` | Room Owner / Admin | Archives study room from public listings. |

---

## 5. Focus Timer (REST API)

### A. Timer Action Endpoint
- **URL**: `/api/timer/action`
- **Method**: `POST`
- **Headers**: `Content-Type: application/json`, `X-CSRF-TOKEN`
- **Request Body**:
```json
{
  "action": "START",           // START | PAUSE | RESUME | COMPLETE | CANCEL
  "plannedMinutes": 25,
  "sessionType": "FOCUS",      // FOCUS | SHORT_BREAK | LONG_BREAK
  "subject": "Operating Systems",
  "roomId": 1                  // optional room association
}
```
- **Response (`200 OK`)**:
```json
{
  "sessionId": 42,
  "status": "IN_PROGRESS",
  "sessionType": "FOCUS",
  "plannedMinutes": 25,
  "activeSeconds": 0,
  "pausedSeconds": 0,
  "remainingSeconds": 1500,
  "running": true,
  "startTime": "2026-10-31T20:00:00",
  "subject": "Operating Systems"
}
```

### B. Current State Endpoint
- **URL**: `/api/timer/state`
- **Method**: `GET`
- **Response**: Returns current `TimerStateResponse` for the authenticated user.

---

## 6. Task Management

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/tasks` | Authenticated | View task manager; supports `?status=...` and `?priority=...`. |
| `POST`| `/tasks/create` | Authenticated | Creates task with title, subject, priority, due date, notes. |
| `POST`| `/tasks/{id}/edit` | Task Owner | Updates task details. |
| `POST`| `/tasks/{id}/toggle` | Task Owner | Toggles task between `PENDING` and `COMPLETED`. |
| `POST`| `/tasks/{id}/delete` | Task Owner | Deletes task permanently. |

---

## 7. Administrator Console (`ROLE_ADMIN` Only)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/admin` | Admin dashboard with KPI metrics, user management, and room moderation. |
| `POST`| `/admin/users/{id}/toggle-status` | Toggles student account between `ACTIVE` and `SUSPENDED`. |
| `POST`| `/admin/rooms/{id}/archive` | Forces room archival. |
| `POST`| `/admin/rooms/{id}/delete` | Permanently deletes a study room and cascades memberships/messages. |

---

## 8. Real-Time WebSocket & STOMP Channels

- **Handshake Endpoint**: `/ws` (with SockJS fallback)
- **Application Prefix**: `/app`
- **Topic Broker Prefix**: `/topic`

### Channels
1. **Send Message**:
   - Client sends to: `/app/chat.sendMessage/{roomId}`
   - Server broadcasts to: `/topic/room/{roomId}`
2. **User Presence Join**:
   - Client sends to: `/app/chat.addUser/{roomId}`
   - Server announces join alert to: `/topic/room/{roomId}`
