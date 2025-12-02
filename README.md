# Classroom Booking System for IIT Tirupati
### Object-Oriented Programming (CS203M) Course Project

**Developed By:**
*   **Ch Pranav Tej** (CS24B057)
*   **Shivam Purve** (CS24B055)
*   *2nd Year, Computer Science & Engineering, IIT Tirupati*

---

## 1. Abstract & Problem Statement

### The Need
As IIT Tirupati expands with new infrastructure, buildings, and academic blocks, the manual management of classroom, lab, and auditorium bookings has become inefficient. The lack of a centralized system leads to:
*   Booking conflicts (double booking).
*   Lack of transparency regarding room availability.
*   Inefficient resource utilization (empty rooms vs. overcrowded study spaces).
*   Administrative overhead in approving simple requests.

### The Solution
We have developed a robust, dynamic, and automated **Classroom Booking System**. This full-stack application utilizes strict **Object-Oriented Programming (OOP)** principles to model the real-world hierarchy of the campus (Campus -> Building -> Floor -> Room) and the hierarchy of users (Admin -> Faculty -> Student). It features an interactive UI, real-time conflict detection, and a priority-based booking overriding system.

---

## 2. Technology Stack

The project uses a standard Enterprise Java stack, ensuring reliability and adherence to strict typing and OOP standards.

*   **Language:** Java 21 (JDK 17+)
*   **Backend Framework:** Spring Boot 3.1.5 (Web, JPA)
*   **Database:** H2 Database (In-Memory/File-based persistence) - chosen for portability.
*   **Frontend:** HTML5, CSS3, Bootstrap 5, Vanilla JavaScript (ES6).
*   **Build Tool:** Maven.
*   **Architecture:** MVC (Model-View-Controller) & REST API.

---

## 3. Object-Oriented Programming Implementation

This project was built specifically to demonstrate the four pillars of OOP. The backend architecture maps real-world entities directly to Java classes.

### A. Inheritance
We utilized inheritance to promote code reusability and logical grouping of entities.

*   **User Hierarchy:**
    *   **Base Class:** `User` (Abstract Class). Contains common fields like `id`, `name`, `email`, `password`, `role`.
    *   **Derived Classes:**
        *   `Student`: Adds specific fields like `studentId` (Roll No), `branch`, `program`.
        *   `Faculty`: Adds `employeeId`, `department`.
        *   `Admin`: Adds administrative privileges.
    *   *Benefit:* We write login logic once for `User`, and it works for all specific types.

*   **Room Hierarchy:**
    *   **Base Class:** `Room` (Abstract Class). Contains `capacity`, `name`, `resources`.
    *   **Derived Classes:**
        *   `Classroom`: Adds boolean flags like `hasSmartBoard`.
        *   `Lab`: Adds `labType` (Computer, Hardware, etc.).
    *   *Benefit:* The booking system treats everything as a `Room`, but we can store specific details for Labs vs Classrooms.

### B. Polymorphism
The system uses polymorphism to handle different behaviors dynamically.

*   **Booking Overrides:** The logic to check for booking clashes uses polymorphic checks on the `User` role.
    *   Admin > Faculty > Student.
    *   The system treats the requester as a generic `User` initially, then checks the specific instance (`instanceof` or Role Enum) to determine priority behavior (e.g., A Faculty can override a Student's booking, but not another Faculty's).
*   **Repository Layer:** We use Spring Data JPA's `JpaRepository`, which is a polymorphic interface allowing us to perform CRUD operations on any entity type without rewriting SQL queries.

### C. Encapsulation
Data integrity is maintained through strict encapsulation.

*   **Private Fields:** All model fields (e.g., `password`, `bookingStatus`) are `private`.
*   **Accessors:** Interaction happens strictly through public Getters and Setters.
*   **Service Layer Logic:** The controller does not modify database entities directly. It calls the `BookingService`, which encapsulates the business logic (like checking for time conflicts) before modifying the data.

### D. Composition & Aggregation
We modeled the physical infrastructure of IIT Tirupati using Composition (Strong "Has-A" relationship).

*   **Campus Structure:**
    *   A `Campus` is composed of `Building`s.
    *   A `Building` is composed of `Floor`s.
    *   A `Floor` is composed of `Room`s.
    *   *Implementation:* Deleting a `Building` cascades and deletes all its `Floor`s and `Room`s automatically (Cascading Delete), demonstrating strong composition.

---

## 4. System Architecture & Data Flow

1.  **Client Layer:** The user interacts with the `index.html` interface. JavaScript fetches data asynchronously using `fetch()`.
2.  **Controller Layer (`APIController`):** Exposes REST endpoints (e.g., `/api/book`, `/api/login`). It accepts JSON requests and deserializes them into Java Objects.
3.  **Service Layer:** Contains the core logic:
    *   Validating time slots (8 AM - 12 AM).
    *   Checking Booking Overlaps.
    *   Handling Priority Overrides.
4.  **Repository Layer:** Interfaces extending `JpaRepository` that interact with the H2 Database.
5.  **Database:** Stores `Users`, `Buildings`, `Rooms`, and `Bookings`.

---

## 5. Key Features

### 1. Dynamic Infrastructure Management (Admin)
*   Admins can create Buildings dynamically.
*   Floors are auto-generated based on the building height.
*   Rooms can be added to specific floors with capacity and resource details.
*   **Delete Functionality:** Granular deletion of Rooms, Floors, or entire Buildings.

### 2. Hierarchy-Based Booking Engine
*   **Student:** Can book available slots. Requests go to "Pending" or "Confirmed".
*   **Faculty:** Can book slots. If a student has already booked the slot, the Faculty has the **Override** option to take that slot (Student gets bumped).
*   **Admin:** Has supreme priority. Can override both Faculty and Students.

### 3. Interactive UI
*   **Split-Screen Login:** Professional UI with Campus imagery and Developer Credits.
*   **Campus Map:** A tree-view visualization of every building, floor, and room.
*   **Dashboard:** Real-time statistics (Total Rooms, Pending Requests) using "Flashcards".

### 4. Resource Management
*   When booking, users can select a checklist of required resources (Smartboard, Mic, Projector).
*   Admins can view these requirements before approving.

---

## 6. Project Directory Structure

```text
ClassroomBookingSystem/
├── pom.xml                     # Maven Dependencies
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/iit/booking/
│   │   │       ├── BookingApplication.java    # Main Entry Point
│   │   │       ├── controller/
│   │   │       │   └── APIController.java     # REST Endpoints
│   │   │       ├── model/
│   │   │       │   ├── User.java              # Abstract Base Class
│   │   │       │   ├── Student.java           # Child Class
│   │   │       │   ├── Faculty.java           # Child Class
│   │   │       │   ├── Admin.java             # Child Class
│   │   │       │   ├── Building.java          # Infrastructure
│   │   │       │   ├── Floor.java
│   │   │       │   ├── Room.java              # Abstract Base Class
│   │   │       │   ├── Classroom.java
│   │   │       │   ├── Lab.java
│   │   │       │   └── Booking.java           # Booking Entity
│   │   │       ├── model/enums/
│   │   │       │   ├── UserType.java
│   │   │       │   └── BookingStatus.java
│   │   │       ├── repo/                      # Data Access Layer
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── BookingRepository.java
│   │   │       │   ├── RoomRepository.java
│   │   │       │   ├── FloorRepository.java
│   │   │       │   └── BuildingRepository.java
│   │   │       └── service/
│   │   │           └── BookingService.java    # Business Logic
│   │   └── resources/
│   │       ├── application.properties         # DB Config
│   │       └── static/
│   │           └── index.html                 # Single Page Application (Frontend)
