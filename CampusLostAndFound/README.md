# Campus Lost and Found Management System

A console-based Java application that keeps a central, database-backed record of
lost and found items on a college campus. It is built with core Java (OOP),
JDBC and MySQL, and is designed to be easy to read, run and explain.

---

## 1. Project Title

**Campus Lost and Found Management System** - a console (text menu) application
for reporting, searching, updating and recovering items lost on campus.

## 2. Short Description

Students lose belongings such as ID cards, notebooks, keys and water bottles,
while items found by other students often never reach their owners. This
project stores every lost or found report in a MySQL database and gives users a
simple menu to register items, search for them, update details, mark items as
recovered, delete records and view statistics. All data is stored permanently
in MySQL, so records survive after the program is closed.

## 3. Problem Statement

There is no central place to record items lost or found on campus. Records are
kept on paper notice boards or in chat groups, which are hard to search and
easy to lose. As a result, recovered items are not returned and the same item
is reported again and again. A simple, searchable, persistent record system
solves this problem.

## 4. Objectives

1. Maintain organised records of lost and found items on campus.
2. Allow users to register lost items and items they have found.
3. Enable searching and viewing of registered item records.
4. Implement Create, Read, Update and Delete (CRUD) operations.
5. Track the recovery status of items.
6. Apply Java OOP concepts in a practical application.
7. Implement MySQL database connectivity using JDBC.
8. Design a reliable, maintainable and understandable database-driven program.

## 5. Key Features

* Report a **lost** item (with automatic item ID from MySQL).
* Register a **found** item.
* View all items in a formatted table.
* Search by item ID, partial item name, category, location, report type or status.
* View only lost items or only found items, including their recovery status.
* Update item details with a change summary and confirmation step.
* Mark an item as **recovered** without changing its report type.
* Delete a record only after confirmation.
* View statistics calculated live from the database.
* Input validation and friendly error handling (never crashes on bad input).
* Passwords are kept out of the source code using a `.env` file.

## 6. Technology Stack

| Layer            | Technology                                  |
|------------------|---------------------------------------------|
| Language         | Java 17                                     |
| Concepts         | Core Java OOP (encapsulation, classes, objects) |
| Database         | MySQL 8.0                                   |
| Connectivity     | JDBC (mysql-connector-j 8.4.0)              |
| Build tool       | Apache Maven                                |
| Interface        | Console / terminal (Scanner, System.out)    |
| Version control  | Git and GitHub                              |

The project does **not** use HTML, CSS, JavaScript, a web server, a graphical
interface or any frontend framework.

## 7. Console Application Preview

The output below was captured from an actual run of the program against a
MySQL database loaded with the optional demo data (`database/sample_data.sql`).

**Startup banner and menu:**

```text
====================================================
CAMPUS LOST AND FOUND MANAGEMENT SYSTEM
=======================================

  A Simple Campus Item Recovery System

====================================================

 1. Report a Lost Item
 2. Register a Found Item
 3. View All Items
 4. Search Items
 5. View Lost Items
 6. View Found Items
 7. Update Item Details
 8. Mark Item as Recovered
 9. Delete Item Record
10. View Statistics
11. Exit
----------------------------------------------------
Enter your choice:
```

**View All Items (option 3):**

```text
-- ALL ITEMS --
ID    NAME                   CATEGORY     TYPE   LOCATION               DATE       STATUS
----- ---------------------- ------------ ------ ---------------------- ---------- ----------
6     Calculator             Electronics  FOUND  Lecture Hall 3, las... 2026-10-05 ACTIVE
5     Black Umbrella         Accessories  LOST   Bus stop, main gate    2026-10-04 ACTIVE
4     College ID Card        Documents    FOUND  Canteen, near count... 2026-10-03 RECOVERED
3     USB Pendrive 64GB      Electronics  LOST   Computer Lab 1         2026-10-02 ACTIVE
2     Black Water Bottle     Bottles      FOUND  Block C, Classroom 204 2026-10-01 ACTIVE
1     Blue Wallet            Documents    LOST   Central Library, 2n... 2026-09-28 ACTIVE
Total records: 6
```

**View Statistics (option 10):**

```text
-- STATISTICS --
----------------------------------------------------
Total registered items : 6
Total lost-item reports: 3
Total found-item reports: 3
Active lost items      : 3
Active found items     : 2
Total recovered items  : 1
----------------------------------------------------
```

Notice how the recovered `College ID Card` is still counted as a **FOUND**
report (its report type never changed), and it also appears in the recovered
total. Active found items is 2 because one found item has been recovered.

## 8. Project Structure

```text
CampusLostAndFound/
├── database/
│   ├── schema.sql            # creates the database and items table
│   └── sample_data.sql       # optional demo rows (clearly marked as demo)
├── docs/
│   ├── project-report.md     # full academic report
│   ├── database-design.md    # schema, keys, constraints and key queries
│   ├── oop-concepts.md       # where each OOP concept appears in the code
│   ├── testing.md            # test cases and their real results
│   ├── learning-guide.md     # beginner walkthrough of the code
│   └── viva-questions.md     # 15 likely viva questions with answers
├── src/main/java/com/campus/lostfound/
│   ├── Main.java             # entry point
│   ├── model/
│   │   ├── Item.java         # the item data model (encapsulation)
│   │   └── Statistics.java   # statistics holder
│   ├── dao/
│   │   └── ItemDAO.java      # the only class that runs SQL
│   ├── service/
│   │   └── ItemService.java  # business rules and validation
│   ├── util/
│   │   └── DBConnection.java # JDBC connection + .env configuration
│   └── console/
│       ├── ConsoleMenu.java  # menus, tables and user interaction
│       └── InputValidator.java # reads and validates user input
├── .env.example              # template for local database settings
├── .gitignore                # keeps target/ and .env out of git
├── LICENSE                   # MIT licence
├── pom.xml                   # Maven build file
└── README.md
```

## 9. Prerequisites

* **JDK 17** or newer (`java -version`).
* **MySQL Server 8.0** running locally (`MySQL80` service on Windows).
* **Apache Maven 3.9+** (`mvn -version`) - or an IDE such as IntelliJ IDEA or
  Eclipse that can import a Maven project.
* **Git** (only needed to publish the project on GitHub).

## 10. MySQL Database Setup

1. Start MySQL (on Windows it normally runs as the `MySQL80` service).
2. Open **MySQL Workbench** or a terminal and run the schema script **as an
   administrator (for example `root`)**:

   ```bash
   mysql -u root -p < database/schema.sql
   ```

   Or open `database/schema.sql` in MySQL Workbench and execute it.

3. Create the limited application user. The commented section at the bottom of
   `database/schema.sql` shows the statements:

   ```sql
   CREATE USER IF NOT EXISTS 'campus_app'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON campus_lost_found.* TO 'campus_app'@'localhost';
   FLUSH PRIVILEGES;
   ```

   The application never connects as `root`; it only needs rights on the one
   `campus_lost_found` database.

4. (Optional) Load demonstration data:

   ```bash
   mysql -u root -p campus_lost_found < database/sample_data.sql
   ```

   The application also works correctly with a completely empty database.

## 11. JDBC Configuration

Connection details are **not** hardcoded. They are read (in priority order) from:

1. built-in defaults (`localhost`, port `3306`, database `campus_lost_found`,
   user `campus_app`),
2. the `.env` file in the project root,
3. real operating-system environment variables (`DB_HOST`, `DB_PORT`,
   `DB_NAME`, `DB_USER`, `DB_PASSWORD`).

To configure the project:

```bash
# Windows (PowerShell)
Copy-Item .env.example .env

# macOS / Linux
cp .env.example .env
```

Then edit `.env`:

```text
DB_HOST=localhost
DB_PORT=3306
DB_NAME=campus_lost_found
DB_USER=campus_app
DB_PASSWORD=your_password
```

`.env` is listed in `.gitignore`, so **your real password is never uploaded to
GitHub**. The JDBC connection string is built in
`src/main/java/com/campus/lostfound/util/DBConnection.java`.

## 12. Build and Run Instructions

Compile the project:

```bash
mvn clean package
```

This produces a single runnable jar that already contains the MySQL driver:

```bash
java -jar target/campus-lost-and-found-1.0.0.jar
```

For development you can also run it directly through Maven without packaging:

```bash
mvn compile exec:java
```

Both commands must be run from the project root so that the `.env` file is
found. (The `exec:java` run also works inside IntelliJ IDEA / Eclipse by running
`com.campus.lostfound.Main`.)

## 13. Example Console Workflow

Reporting a lost item and then searching for it by name:

```text
Enter your choice: 1

-- REPORT A LOST ITEM --
Item name               : Silver Watch
Category                : Accessories
Description (optional)  : Fastrack watch with a leather strap
Last seen location      : Sports Ground
Date lost (yyyy-mm-dd)  : 2026-10-08
Contact info (optional) : 9998887770

Item added successfully. Item ID: 7

Enter your choice: 4
...
Enter your choice: 2
Enter item name (partial match is fine): watch

ID    NAME                   CATEGORY     TYPE   LOCATION               DATE       STATUS
----- ---------------------- ------------ ------ ---------------------- ---------- ----------
7     Silver Watch           Accessories  LOST   Sports Ground          2026-10-08 ACTIVE
Total records: 1
```

## 14. Database Schema Overview

One table, `items`, stores every report. `item_id` is generated automatically.

| Column         | Type         | Rule                                   |
|----------------|--------------|----------------------------------------|
| `item_id`      | INT          | primary key, AUTO_INCREMENT            |
| `item_name`    | VARCHAR(100) | required (NOT NULL)                    |
| `category`     | VARCHAR(50)  | required                               |
| `description`  | VARCHAR(500) | optional                               |
| `report_type`  | VARCHAR(10)  | `LOST` or `FOUND` (CHECK constraint)   |
| `location`     | VARCHAR(100) | required                               |
| `report_date`  | DATE         | required                               |
| `contact_info` | VARCHAR(100) | optional                               |
| `status`       | VARCHAR(10)  | `ACTIVE` or `RECOVERED`, default ACTIVE|
| `created_at`   | TIMESTAMP    | set automatically on insert            |
| `updated_at`   | TIMESTAMP    | updated automatically on change        |

Full details are in [docs/database-design.md](docs/database-design.md).

## 15. Java OOP Concepts Demonstrated

* **Classes and objects** - `Item` is the blueprint; each row becomes an object.
* **Encapsulation** - all `Item` fields are private with public getters/setters.
* **Constructors** - `Item()` to build empty objects while reading input and a
  full constructor for new reports.
* **Methods and responsibility** - each class does one job: model, DAO, service,
  util, console.
* **Abstraction** - the console calls `service.reportLostItem(...)` and never
  knows how SQL works.
* **Static members** - `DBConnection`, `InputValidator` helpers and the
  `Item.TYPE_LOST` / `Item.STATUS_ACTIVE` constants.
* **Constant fields and immutability** - `Statistics` holds `final` values.

Because these are explained with real line-level examples in
[docs/oop-concepts.md](docs/oop-concepts.md), they are easy to demonstrate
during an evaluation.

## 16. CRUD Operations Implemented

| Operation | Menu option            | Java method                        | SQL                     |
|-----------|------------------------|------------------------------------|-------------------------|
| Create    | 1 Report Lost / 2 Found| `ItemDAO.addItem()`                | `INSERT`                |
| Read      | 3, 5, 6                | `getAllItems()`, `getItemsByReportType()` | `SELECT`         |
| Search    | 4                      | `ItemDAO.searchItems()`            | `SELECT ... LIKE`       |
| Update    | 7                      | `ItemDAO.updateItem()`             | `UPDATE`                |
| Recover   | 8                      | `ItemDAO.markAsRecovered()`        | `UPDATE ... WHERE ACTIVE`|
| Delete    | 9                      | `ItemDAO.deleteItem()`             | `DELETE`                |
| Statistics| 10                     | `ItemDAO.getStatistics()`          | `SELECT` with `SUM(CASE...)` |

## 17. Testing Instructions

The project was compiled and run against a local MySQL 8.0.43 server. The test
cases, the exact commands and the observed results are recorded honestly in
[docs/testing.md](docs/testing.md). To repeat them:

1. Set up the database as in section 10.
2. Build with `mvn clean package`.
3. Run `java -jar target/campus-lost-and-found-1.0.0.jar` and follow the menu,
   or pipe a prepared list of menu inputs for repeatable tests.

Tested scenarios include: empty database, adding lost/found items, all search
types, partial search, update with confirmation, recovery (including blocking a
second recovery), delete with confirmation, pagination, input validation and
database connection failure.

## 18. Common Errors and Troubleshooting

| Message / symptom                                             | Likely cause and fix                                                                 |
|--------------------------------------------------------------|--------------------------------------------------------------------------------------|
| `Access denied for user 'campus_app'@'localhost'`            | Wrong `DB_USER` / `DB_PASSWORD` in `.env`, or the user was not created.              |
| `Unable to connect to the database...`                       | MySQL is not running, or the port/database name in `.env` is wrong.                  |
| `Table 'items' does not exist...`                            | Run `database/schema.sql` once as an administrator.                                  |
| `No suitable driver found for jdbc:mysql://...`              | The MySQL driver is missing from the classpath. Run with `mvn exec:java` or the packaged jar. |
| `Database password is not configured`                        | `.env` is missing or `DB_PASSWORD` is empty. Copy `.env.example` to `.env`.          |
| Garbled characters in the console                             | Use a UTF-8 capable terminal (Windows Terminal / the IDE console).                   |

Set the environment variable `APP_DEBUG=true` to print the full technical error
message and stack trace when diagnosing a problem. The password value itself is
never printed.

## 19. Future Enhancements

* Add a simple login so students and staff see different options.
* Store and display a photo of each item.
* Match lost and found reports automatically using name and category.
* Add email/SMS notifications when a matching item is registered.
* Export reports to CSV or PDF.
* Build a graphical (Swing/JavaFX) or web interface on top of the same service
  layer.
* Add a JUnit test suite for the service layer.

## 20. Academic Project Note

This project was developed as a college assignment to demonstrate Java OOP,
JDBC and DBMS concepts in a real, working application. It was compiled and run
successfully against MySQL. All console output shown in this README was
captured from actual runs; nothing is a mock-up or a screenshot.

The source code favours clarity over cleverness so that a beginner can read and
explain every part of it. It is published under the MIT licence (see
[LICENSE](LICENSE)), which allows others to study, reuse and modify the code as
long as the copyright notice is kept.
