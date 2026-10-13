# Project Report

## Campus Lost and Found Management System

---

## 1. Project Title

**Campus Lost and Found Management System** - a console-based Java application
for recording, searching, updating and recovering lost and found items on a
college campus, backed by a MySQL database.

## 2. Abstract

Students frequently lose personal belongings on campus, while items found by
other students often never reach their owners because there is no central
record. This project delivers a console application that stores every lost and
found report in a MySQL database and allows users to register items, search for
them, update their details, mark them as recovered, delete records and view
statistics. The program is written in Java 17 using object-oriented design and
connects to MySQL through JDBC. It demonstrates practical use of classes,
objects, encapsulation, prepared statements, CRUD operations and database
constraints in a single, readable codebase.

## 3. Problem Statement

Records of lost and found items on campus are kept informally - on notice
boards or in messaging groups. These methods are hard to search, are easily
lost and cannot show whether an item has already been returned. There is no way
to ask "is there a found wallet reported near the library?" or "how many items
are still missing?". A central, searchable, persistent record system is needed.

## 4. Objectives

1. Maintain organised records of lost and found items on campus.
2. Allow users to register lost items and items they have found.
3. Enable searching and viewing of registered item records.
4. Implement Create, Read, Update and Delete (CRUD) operations.
5. Track the recovery status of items.
6. Apply Java OOP concepts in a practical application.
7. Implement MySQL database connectivity using JDBC.
8. Design a reliable, maintainable and understandable database-driven program.

## 5. Scope

**In scope**

* A single-campus console application.
* Lost/found item reporting, searching, updating, recovery, deletion and
  statistics.
* Data stored in one MySQL database.
* Input validation and friendly error messages.

**Out of scope**

* User accounts, login or roles.
* Online/networked multi-user access or a web interface.
* Image upload, notifications or automatic matching of items.
* These are listed as future enhancements.

## 6. Functional Requirements

| ID   | Requirement                                                              |
|------|--------------------------------------------------------------------------|
| FR1  | The system shall let the user report a lost item.                        |
| FR2  | The system shall let the user register a found item.                     |
| FR3  | The system shall assign each item an automatic unique ID.                |
| FR4  | The system shall display all item records in a table.                    |
| FR5  | The system shall search items by ID, name, category, location, type, status. |
| FR6  | The system shall support partial name and location searches.            |
| FR7  | The system shall display lost items and found items separately.          |
| FR8  | The system shall update item details after confirmation.                 |
| FR9  | The system shall mark an active item as recovered without changing its report type. |
| FR10 | The system shall refuse to recover an item twice.                        |
| FR11 | The system shall delete a record only after confirmation.                |
| FR12 | The system shall show statistics calculated from the database.           |
| FR13 | The system shall validate all input and never crash on bad input.        |

## 7. Non-functional Requirements

| ID   | Requirement                                                              |
|------|--------------------------------------------------------------------------|
| NFR1 | The data shall persist in MySQL between program runs.                    |
| NFR2 | The code shall follow Java naming conventions and be readable.           |
| NFR3 | Businesses rules shall be separated from the console presentation.       |
| NFR4 | Passwords shall not be hardcoded into the source code.                   |
| NFR5 | SQL shall use `PreparedStatement` to prevent SQL injection.              |
| NFR6 | Resources (connection, statement, result set) shall be closed automatically. |
| NFR7 | The program shall run on the console of Windows and IDEs.                |

## 8. Hardware and Software Requirements

**Hardware**

* Any PC / laptop with at least 2 GB RAM and 200 MB free disk space.

**Software**

* JDK 17 or newer.
* MySQL Server 8.0.
* Apache Maven 3.9+ (or an IDE with Maven support).
* MySQL Workbench or the `mysql` client (for setup).

## 9. System Architecture

The application uses a layered (n-tier) design:

```text
   User
     |
     v
+-----------------------------------------------+
| Console layer                                  |
|   Main.java, ConsoleMenu.java, InputValidator |
+-----------------------------------------------+
     |  calls service methods
     v
+-----------------------------------------------+
| Service layer                                  |
|   ItemService.java  (business rules)           |
+-----------------------------------------------+
     |  calls DAO methods
     v
+-----------------------------------------------+
| Data access layer                              |
|   ItemDAO.java  (SQL, PreparedStatement)       |
+-----------------------------------------------+
     |  JDBC
     v
+-----------------------------------------------+
| Util layer                                     |
|   DBConnection.java  ->  MySQL database        |
+-----------------------------------------------+
```

The model layer (`Item`, `Statistics`) is used by every other layer to carry
data. Separation of responsibilities means the console contains no SQL and the
DAO contains no menu code.

## 10. Use Cases

| Actor | Use case               | Precondition                  | Main flow                                                  |
|-------|------------------------|-------------------------------|------------------------------------------------------------|
| User  | Report lost item       | Database reachable            | Choose 1, enter details, record saved with type LOST.      |
| User  | Register found item    | Database reachable            | Choose 2, enter details, record saved with type FOUND.     |
| User  | Search items           | Records may exist             | Choose 4, pick a search field, key in value, see results.  |
| User  | Update item            | Item ID exists                | Choose 7, enter ID, edit fields, confirm, row updated.     |
| User  | Mark item recovered    | Item exists and is ACTIVE     | Choose 8, enter ID, confirm, status becomes RECOVERED.     |
| User  | Delete item            | Item ID exists                | Choose 9, enter ID, confirm, row deleted.                  |
| User  | View statistics        | Database reachable            | Choose 10, totals are displayed.                           |

## 11. Database Design

One database (`campus_lost_found`) with one table (`items`). Full details are
in [database-design.md](database-design.md). Summary:

* Primary key `item_id` (INT, AUTO_INCREMENT).
* Required fields: `item_name`, `category`, `report_type`, `location`,
  `report_date`, `status`.
* Optional fields: `description`, `contact_info`.
* `report_type` restricted to LOST / FOUND, `status` restricted to
  ACTIVE / RECOVERED using CHECK constraints.
* `created_at` and `updated_at` timestamps manage themselves.
* Indexes support searching.

## 12. Table Structure and Field Descriptions

See the table in [database-design.md](database-design.md#2-table-items). Each
field maps directly to a private field in `model/Item.java`.

## 13. Java OOP Concepts Used

Classes and objects, encapsulation, constructors, abstraction, method
overloading, `toString()` overriding, static members/constants, and
final/immutable fields. Each concept is tied to real code in
[oop-concepts.md](oop-concepts.md).

## 14. JDBC Connectivity

* The driver `com.mysql:mysql-connector-j:8.4.0` is declared in `pom.xml`.
* `DBConnection` builds the URL
  `jdbc:mysql://host:port/database?...` and calls `DriverManager.getConnection`.
* Connection details come from `.env` / environment variables, never from
  source code.
* `ItemDAO` uses `PreparedStatement` for every query and `try-with-resources`
  to close connections, statements and result sets automatically.
* `Statement.RETURN_GENERATED_KEYS` is used to read the new `item_id` after an
  insert.

## 15. CRUD Implementation

| Operation | Java method                        | SQL                                   |
|-----------|------------------------------------|---------------------------------------|
| Create    | `ItemDAO.addItem`                  | `INSERT INTO items ...`               |
| Read      | `getAllItems`, `getItemById`, `getItemsByReportType`, `searchItems` | `SELECT` |
| Update    | `updateItem`, `markAsRecovered`    | `UPDATE items SET ...`                |
| Delete    | `deleteItem`                       | `DELETE FROM items WHERE ...`         |

## 16. Input Validation and Exception Handling

* `InputValidator` reads every value as a line and parses it, avoiding the
  classic `nextInt()`/`nextLine()` bug. It loops until the input is valid.
* Required text, valid dates, valid report types/statuses and positive IDs are
  all checked.
* `ItemService` enforces business rules (no future dates, no recovering an
  already recovered item, record must exist before update/delete).
* `SQLException`s are caught in `ConsoleMenu.handleChoice` and turned into a
  friendly message; technical details appear only when `APP_DEBUG=true`.
* `Main` checks the connection and the table at startup and explains how to fix
  configuration problems.

## 17. Testing Approach and Test Cases

Testing was performed by building with Maven and running the packaged jar
against a local MySQL 8.0 server, piping prepared menu inputs for repeatable
tests and checking the database directly. All 22 planned scenarios passed, plus
pagination, CHECK constraints, missing-table startup and the no-change update
path. A real bug (missing `status` in the update flow) was found and fixed.

The complete test cases and observed results are in
[testing.md](testing.md).

## 18. Capturing Screenshots / Console Output

To capture output for a report or presentation:

* **Windows PowerShell:** run the program, select the text in the window, or
  redirect output with
  `java -jar target/campus-lost-and-found-1.0.0.jar > output.txt`.
* **IDE (IntelliJ/Eclipse):** run `com.campus.lostfound.Main` and copy from the
  Console tab.
* **Screenshot:** `Win + Shift + S` (Windows) to capture a screen region.

Example captured output is already included in the top-level
[README.md](../README.md#7-console-application-preview) and in
[testing.md](testing.md).

## 19. Limitations

* Single-user, single-campus console application.
* No login or access control.
* No image storage, notifications or automatic item matching.
* Search is case-insensitive on text via `LIKE`, not full-text search.
* No automated unit-test suite.

## 20. Future Enhancements

* Login and roles for students, staff and administrators.
* Item photos and automatic lost/found matching.
* E-mail/SMS notifications when a matching item is registered.
* CSV/PDF export and reports.
* A graphical (Swing/JavaFX) or web front end over the same service layer.
* A JUnit test suite.

## 21. Conclusion

The Campus Lost and Found Management System meets all of its objectives. It is
a genuine console-based Java program that stores data persistently in MySQL
through JDBC, applies object-oriented design with clear separation of
responsibilities, and provides complete CRUD functionality with validation and
graceful error handling. It compiles and runs successfully, has been tested
against a real MySQL server, and is documented well enough to be understood and
explained by a beginner. It solves a real campus problem while demonstrating
the OOP and DBMS concepts required by the course.
