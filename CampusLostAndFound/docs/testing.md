# Testing Report

This document records how the application was tested and the **actual results**
that were observed. Nothing here is invented; where a scenario could not be
tested, it is stated honestly.

## 1. Test environment

| Item          | Value                                                        |
|---------------|--------------------------------------------------------------|
| Operating system | Windows 11                                               |
| Java          | OpenJDK 17.0.19 (Temurin)                                    |
| Build tool    | Apache Maven 3.9.9                                           |
| Database      | MySQL Community Server 8.0.43                                |
| Driver        | mysql-connector-j 8.4.0                                      |
| Build command | `mvn clean package` -> `target/campus-lost-and-found-1.0.0.jar` |

The application was built with Maven and executed as the packaged jar. Repeatable
tests were run by piping prepared menu inputs into the program and reading the
console output. The database was checked directly with the `mysql` client for
the operations that change data.

## 2. Summary of results

All 22 scenarios below were executed. **All passed.**

| # | Scenario                                   | Result |
|---|--------------------------------------------|--------|
| 1 | Start the application                       | PASS   |
| 2 | Connect to MySQL                            | PASS   |
| 3 | Add a lost item                             | PASS   |
| 4 | Add a found item                            | PASS   |
| 5 | View all records                            | PASS   |
| 6 | Search by item ID                           | PASS   |
| 7 | Search by partial item name                 | PASS   |
| 8 | Search by category                          | PASS   |
| 9 | View lost-item records                      | PASS   |
| 10| View found-item records                     | PASS   |
| 11| Update item details                         | PASS   |
| 12| Mark an item as recovered                   | PASS   |
| 13| Report type unchanged after recovery        | PASS   |
| 14| Recovered count updates                     | PASS   |
| 15| Delete a record (with confirmation)         | PASS   |
| 16| Reject invalid IDs                          | PASS   |
| 17| Handle empty required fields                | PASS   |
| 18| Handle invalid date formats                 | PASS   |
| 19| Handle incorrect menu input                 | PASS   |
| 20| Handle an empty database                    | PASS   |
| 21| Handle a database connection failure        | PASS   |
| 22| Records persist after restarting            | PASS   |

Extra scenarios also tested: pagination (>10 rows), database CHECK constraints,
the "table missing" startup message, and the "no changes" update path.

## 3. Detailed test cases

### TC-01 - Start and connect

* **Action:** `java -jar target/campus-lost-and-found-1.0.0.jar`
* **Expected:** banner and menu appear.
* **Observed:**

  ```text
  Connecting to MySQL...
  Database connection established. (host=127.0.0.1, port=3307, database=campus_lost_found, user=campus_app)
  ====================================================
  CAMPUS LOST AND FOUND MANAGEMENT SYSTEM
  ...
  ```
* **Result:** PASS

### TC-02 - Add a lost item (menu 1)

* **Input:**

  ```text
  1
  Blue Wallet
  Documents
  Blue leather wallet with the student ID inside
  Central Library, 2nd floor
  2026-09-28
  rahul@student.campus.edu
  ```
* **Observed:** `Item added successfully. Item ID: 1`
* **Result:** PASS - MySQL returned the generated ID via `getGeneratedKeys()`.

### TC-03 - Add a found item (menu 2)

* **Input:** same shape with report type FOUND.
* **Observed:** `Item added successfully. Item ID: 2`
* **Result:** PASS

### TC-04 - View all items (menu 3)

* **Observed:**

  ```text
  ID    NAME                   CATEGORY     TYPE   LOCATION               DATE       STATUS
  2     Black Water Bottle     Bottles      FOUND  Block C, Classroom 204 2026-10-01 ACTIVE
  1     Blue Wallet            Documents    LOST   Central Library, 2n... 2026-09-28 ACTIVE
  Total records: 2
  ```
* **Result:** PASS - newest first, long values truncated so columns stay aligned.

### TC-05 to TC-10 - Search (menu 4, submenus 1-6)

Searching a two-row database:

| Submenu                         | Input    | Observed                        | Result |
|---------------------------------|----------|---------------------------------|--------|
| 1 by ID                         | `1`      | Blue Wallet (1 record)          | PASS   |
| 2 by name (partial)             | `wallet` | Blue Wallet (1 record)          | PASS   |
| 3 by category                   | `Documents` | Blue Wallet (1 record)       | PASS   |
| 4 by location                   | `library`| Blue Wallet (1 record)          | PASS   |
| 5 by report type                | `LOST`   | Blue Wallet (1 record)          | PASS   |
| 6 by status                     | `RECOVERED` | `No matching records found.` | PASS   |

Partial name search (`wallet`) matched `Blue Wallet`, proving `LIKE %...%`
works.

### TC-11 - Update item details (menu 7)

* **Input for item 1:** new name `Wallet Updated`, new description,
  report type `FOUND`, new date `2026-10-06`, others left blank to keep.
* **Expected:** show a change summary and ask for confirmation.
* **Observed:**

  ```text
  Proposed changes:
    - Item name: Blue Wallet -> Wallet Updated
    - Description: Blue leather wallet with the student ID inside -> A blue wallet found again in class
    - Report type: LOST -> FOUND
    - Report date: 2026-09-28 -> 2026-10-06

  Save these changes? (Y/N): y
  Item updated successfully. Item ID: 1
  ```
* **Result:** PASS

> **Bug found and fixed during testing:** the first version of the update flow
> did not copy the item's `status` into the proposed record, so the service
> validation rejected every update with "Invalid status". The console now sets
> `proposed.setStatus(current.getStatus())` before saving, and the test above
> passes. This bug demonstrates the value of running real tests.

### TC-12 / TC-13 / TC-14 - Recover (menu 8)

* **Action:** recover item 1 (currently FOUND / ACTIVE), confirm with `y`.
* **Observed:**

  ```text
  Item marked as recovered successfully.
  (The report type stays FOUND, only the status changed.)
  ```
* **Then:** recover item 1 again.

  ```text
  Item ID 1 has already been recovered. No changes were made.
  ```
* **Statistics afterwards:**

  ```text
  Total registered items : 3
  Total lost-item reports: 1
  Total found-item reports: 2
  Active lost items      : 1
  Active found items     : 1
  Total recovered items  : 1
  ```
* **Result:** PASS - the recovered item still counts as a FOUND report (found
  total 2 includes it) and also appears once in the recovered total. A second
  recovery is refused.

### TC-15 - Delete a record (menu 9)

* **Action:** delete item 2, first answer `n`, then answer `y`.
* **Observed:**

  ```text
  Delete this record permanently? (Y/N): n
  Operation cancelled.
  ...
  Delete this record permanently? (Y/N): y
  Item record deleted successfully. Item ID: 2
  ```
* **Result:** PASS - "View All Items" afterwards showed the row was gone.

### TC-16 to TC-19 - Validation

| Scenario              | Input              | Observed                                                         | Result |
|-----------------------|--------------------|-----------------------------------------------------------------|--------|
| Invalid menu choice   | `abc`, then `99`   | `Invalid choice. Please enter a number from 1 to 11.`           | PASS   |
| Empty item name       | (blank)            | `Item name cannot be empty.` (asked again)                      | PASS   |
| Empty category        | (blank)            | `Category cannot be empty. Examples: Documents, Electronics...` | PASS   |
| Invalid date          | `2026-13-45`       | `Invalid date. Please use the format yyyy-mm-dd...`             | PASS   |
| Future report date    | `2099-01-01`       | `Report date cannot be in the future.`                          | PASS   |
| Update nonexistent ID | `999`              | `Item not found. Please check the item ID.`                     | PASS   |
| Recover nonexistent ID| `999`              | `Item not found. Please check the item ID.`                     | PASS   |
| Delete nonexistent ID | `999`              | `Item not found. Please check the item ID.`                     | PASS   |

### TC-20 - Empty database

* **Action:** view all items and statistics before any record exists.
* **Observed:**

  ```text
  No item records found. Report a lost item or register a found one to get started.
  ...
  Total registered items : 0
  ...
  ```
* **Result:** PASS - no crash, no empty table, statistics show zeros.

### TC-21 - Database connection failure

* **Action:** run with a wrong password.
* **Observed:**

  ```text
  Unable to connect to the database. Please check your MySQL configuration.
  Configuration: host=127.0.0.1, port=3307, database=campus_lost_found, user=campus_app
  Reason: Access denied for user 'campus_app'@'localhost' (using password: YES)

  How to fix this:
    1. Make sure MySQL is running ...
    2. Copy .env.example to .env and set DB_USER and DB_PASSWORD.
    3. Run database/schema.sql once ...
  ```
* **Result:** PASS - the program exits cleanly and **never prints the password**.

### TC-22 - Persistence after restart

* **Action:** close the program, start it again, run "View All Items".
* **Observed:** the same records were present, including the edited and
  recovered item; the database itself showed:

  ```text
  item_id  item_name         report_type  status     updated_at
  1        Wallet Updated    FOUND        RECOVERED  2026-10-10 13:52:23
  3        Test Item         LOST         ACTIVE     2026-10-10 13:51:18
  ```
* **Result:** PASS - data is stored in MySQL, not in memory, and `updated_at`
  changed after the edit.

## 4. Extra tests

* **Pagination** - with 14 rows, "View All Items" showed 10 rows, asked
  `Press Enter for next page, or type q to stop:`, then showed the remaining 4
  and `Total records: 14`. Typing `q` stopped early. Both PASS.
* **Database CHECK constraints** - direct SQL inserts with `report_type='MAYBE'`
  and `status='LOST'` were rejected:

  ```text
  ERROR 3819: Check constraint 'chk_items_report_type' is violated.
  ERROR 3819: Check constraint 'chk_items_status' is violated.
  ERROR 1292: Incorrect date value: 'not-a-date' for column 'report_date'
  ```
  PASS - the database itself protects the data.
* **Missing table on startup** - after renaming the table away, starting the
  program printed:

  ```text
  The database structure could not be verified.
  Details: Table 'items' does not exist. Please run database/schema.sql once to create the database structure.
  ```
  PASS.
* **Update with no changes** - pressing Enter for every field printed
  `No changes were made.` and saved nothing. PASS.

## 5. Not tested

* No automated JUnit suite is included; testing was manual/repeatable through
  piped console input. Adding JUnit tests for `ItemService` is listed as a
  future enhancement.
* Testing used a local MySQL instance created for this project. Behaviour on
  other MySQL versions is expected to be identical because only standard SQL
  and the official JDBC driver are used, but it was not separately verified.
