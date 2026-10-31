# Focusroom V2 — Virtual Study Room Platform

![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-21%20%7C%2025%20LTS-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-green.svg)
![Tests](https://img.shields.io/badge/tests-18%2F18%20passing-success.svg)

Focusroom V2 is a virtual study room web platform developed for university students, software engineering coursework, and DBMS laboratory examinations. It combines digital collaborative study spaces, synchronized Pomodoro focus sessions, task tracking, real-time messaging, and comprehensive productivity analytics backed by a relational MySQL database.

---

## 1. Project Highlights

- **Java-First Spring Boot Architecture**: Structured multi-tiered architecture with Controller, Service, Repository, Entity, and DTO layers following strict Object-Oriented Programming (OOP) and Clean Architecture principles.
- **Persistent MySQL Database**: True relational database implementation featuring 7 normalized tables (up to BCNF), composite indexes, foreign key cascading constraints, check constraints, and native aggregate queries.
- **Synchronized Focus Engine**: Pomodoro timer with authoritative server-side state tracking. Paused time is strictly isolated and never counted toward active study hours.
- **Collaborative Virtual Rooms**: Students can create or join public and password-protected private study rooms with capacity enforcement, participant tracking, and live WebSocket STOMP chat.
- **Dark-First Modern UI**: Engineered with custom CSS design tokens, glassmorphism, responsive sidebar, Lucide icons, and Chart.js analytics visualizations.
- **Enterprise Security**: Spring Security 6 with BCrypt password hashing, session expiration handling, CSRF protection, and Role-Based Access Control (`ROLE_USER`, `ROLE_ADMIN`).

---

## 2. Technology Stack

| Component | Technology | Version / Notes |
| :--- | :--- | :--- |
| **Language** | Java (JDK) | Java 21 / 25 LTS Compatible |
| **Backend Framework** | Spring Boot | 3.3.5 |
| **MVC & Web** | Spring MVC & Thymeleaf | Thymeleaf 3.1 with Spring Security 6 dialect |
| **Persistence / ORM** | Spring Data JPA / Hibernate | Hibernate ORM 6.5 |
| **Database** | MySQL | MySQL Community Server 8.0+ |
| **Security** | Spring Security | BCrypt password hashing & DaoAuthenticationProvider |
| **Real-Time** | Spring WebSocket & STOMP | SockJS fallback protocol |
| **Build System** | Apache Maven | 3.9+ |
| **Frontend Libraries**| Vanilla JS, Lucide, Chart.js| Modern responsive dark theme (No Node/React dependency) |
| **Testing** | JUnit 5, Mockito, Spring Security Test | 18 automated unit and integration tests |

---

## 3. Architecture & Directory Structure

```
D:\Focusroom\
├── database/
│   ├── schema.sql                   # Full DDL database creation script
│   └── sample-data.sql              # Seed dataset with accounts and sessions
├── docs/
│   ├── ER-diagram.md                # Entity-Relationship diagram & cardinality
│   ├── DBMS-concepts.md             # In-depth DBMS laboratory viva reference
│   └── API-documentation.md         # Complete REST and WebSocket endpoint spec
├── src/
│   ├── main/
│   │   ├── java/com/focusroom/
│   │   │   ├── FocusroomApplication.java
│   │   │   ├── config/              # SecurityConfig, WebSocketConfig
│   │   │   ├── controller/          # MVC controllers & REST endpoints
│   │   │   ├── dto/                 # Data transfer objects & form bindings
│   │   │   ├── entity/              # JPA relational entities & Enums
│   │   │   ├── exception/           # Custom domain exceptions & GlobalExceptionHandler
│   │   │   ├── repository/          # Spring Data JPA repositories & JPQL queries
│   │   │   ├── security/            # CustomUserDetails & CustomUserDetailsService
│   │   │   └── service/             # Business logic interfaces & implementations
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       │   ├── css/focusroom.css# Dark productivity design system
│   │       │   └── js/              # timer.js, room-chat.js
│   │       └── templates/           # Thymeleaf layouts & pages
│   └── test/
│       ├── java/com/focusroom/      # Unit, service, and security integration tests
│       └── resources/
│           └── application.properties# Isolated H2 test database configuration
├── .env.example                     # Sample database environment variables
├── .gitignore                       # Git exclusions
├── pom.xml                          # Maven build dependencies
└── README.md                        # Master documentation
```

---

## 4. Setup and Installation

### Step 1: Clone or Navigate to the Workspace
```powershell
cd D:\Focusroom
```

### Step 2: Configure Environment Variables
Copy `.env.example` to `.env` or set system environment variables:
```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=focusroom_db
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
SERVER_PORT=8080
```

### Step 3: Initialize the MySQL Database
Open your MySQL terminal or workbench and execute:
```sql
SOURCE D:/Focusroom/database/schema.sql;
SOURCE D:/Focusroom/database/sample-data.sql;
```

*Default Seed Accounts (Password for all: `password123`):*
- **Administrator**: `admin` (`admin@focusroom.edu`)
- **Student 1**: `alex_j` (`alex@focusroom.edu`)
- **Student 2**: `sarah_m` (`sarah@focusroom.edu`)

### Step 4: Run the Application
Run via the Maven wrapper or installed Maven:
```powershell
mvn spring-boot:run
```

Once started, open your web browser and navigate to:
**`http://localhost:8080`**

---

## 5. Automated Test Suite

Run the full automated test suite containing 18 unit, service, repository, and security integration tests:
```powershell
mvn test
```

### Tested Edge Cases:
- Duplicate registration rejection (duplicate username & duplicate email)
- Password confirmation mismatch validation
- Room participant capacity enforcement (`RoomFullException`)
- Private room access code verification
- Strict user task isolation (preventing unauthorized cross-user modifications)
- Authoritative Pomodoro pause/resume intervals and study duration calculation
- Role-based security access control (403 Forbidden for non-admins on `/admin`)

---

## 6. DBMS Laboratory Viva Reference

Focusroom V2 was specifically designed to demonstrate core database management concepts in an academic examination context:

1. **Relational Schema Design**: 7 normalized relations (3NF and BCNF compliance).
2. **Integrity Constraints**: Primary keys, composite unique constraints, check constraints, and cascading foreign keys.
3. **Complex Joins**: `LEFT JOIN` with `GROUP BY` to discover rooms and count active participants.
4. **Aggregate Queries**: `SUM()`, `COUNT()`, `AVG()` calculating study durations, streaks, and completion rates.
5. **GROUP BY & HAVING**: Identifying academic subjects exceeding threshold study durations.
6. **Nested Subqueries**: Retrieving sessions exceeding the student's personal average session duration.
7. **Transactions & ACID Properties**: Multi-table persistence guaranteed by Spring `@Transactional` and MySQL InnoDB redo logs.

*For complete SQL code snippets and theoretical explanations, review [docs/DBMS-concepts.md](docs/DBMS-concepts.md).*
