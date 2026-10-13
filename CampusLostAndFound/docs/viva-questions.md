# Viva Questions and Answers

Fifteen likely viva questions with short, accurate answers based on the code
that is actually implemented in this project.

---

**1. What does your project do?**

It is a console-based Java application that records lost and found items on a
campus in a MySQL database. It supports reporting items, searching, updating,
marking items as recovered, deleting records and viewing statistics.

---

**2. Which technologies did you use and why?**

Core Java 17 for OOP, JDBC to connect Java to the database, MySQL 8 to store
the data persistently, and Maven to manage the MySQL driver and build the
project. The interface is the console, so no web or GUI technology is needed.

---

**3. What is the difference between a lost item and a found item in your system?**

Both are stored in the same `items` table. The difference is the `report_type`
column, which is either `LOST` or `FOUND`. This avoids having two nearly
identical code paths.

---

**4. What are `report_type` and `status` and how are they different?**

`report_type` says whether the item was reported lost or found (it never
changes). `status` says whether the item is still `ACTIVE` or has been
`RECOVERED`. Recovering an item changes only `status`; that is why a recovered
found item is still counted as a found report.

---

**5. Where is encapsulation used?**

In the `Item` class. All its fields are `private` and are read or changed only
through public getters and setters. The service layer uses the setters to
normalise and validate the values before they reach the database.

---

**6. What is the role of the DAO class?**

`ItemDAO` (Data Access Object) is the only class that contains SQL. It inserts,
reads, updates and deletes rows using `PreparedStatement`, and converts rows
into `Item` objects. This keeps SQL out of the console and service classes.

---

**7. What is JDBC?**

JDBC (Java Database Connectivity) is the standard Java API used to connect to
and query a database. In this project `DBConnection.getConnection()` opens a
connection with `DriverManager.getConnection(...)` and the DAO uses
`PreparedStatement` and `ResultSet`.

---

**8. What is a `PreparedStatement` and why did you use it?**

A `PreparedStatement` is a parameterised SQL statement with `?` placeholders.
Values are set with `setString`, `setInt`, etc. It prevents SQL injection
because user input is never concatenated into the SQL string, and it handles
data types safely.

---

**9. What is a primary key in your table?**

`item_id`. It is an `INT` with `AUTO_INCREMENT`, so MySQL generates a unique ID
for every new report. The program reads the generated ID back using
`Statement.RETURN_GENERATED_KEYS` and `getGeneratedKeys()`.

---

**10. How does the program generate item IDs?**

It does not generate them itself. It lets MySQL assign the next
`AUTO_INCREMENT` value during the `INSERT`, then asks the database for the
generated key. This avoids race conditions and keeps IDs unique.

---

**11. How does your search work?**

Menu option 4 lets the user search by ID, name, category, location, report type
or status. `ItemDAO.searchItems()` builds the SQL dynamically: every non-blank
condition adds an `AND ... ?` clause, and text fields use `LIKE '%value%'` for
partial matches. All values are still bound with `?`.

---

**12. How do you make sure the user cannot crash the program with bad input?**

`InputValidator` reads every value as a line and parses it inside a loop. It
keeps asking until the input is valid (for example a menu number in range, a
real date, a positive ID, LOST/FOUND). The service also checks business rules
and the console catches `IllegalArgumentException` and `SQLException` to print
friendly messages instead of stack traces.

---

**13. How do you handle exceptions?**

The DAO declares `throws SQLException`. `ConsoleMenu.handleChoice()` catches
`IllegalArgumentException` (rule violations) and `SQLException` (database
problems) in one place and prints a plain-language message. `Main` catches
connection errors at startup and prints troubleshooting steps. Technical
details are shown only when the environment variable `APP_DEBUG=true`.

---

**14. Why is the password not in the source code, and how does the program get it?**

The program reads connection settings from a `.env` file (or real environment
variables) through `DBConnection`. The `.env` file is listed in `.gitignore`, so
it is never committed to GitHub. This prevents leaking database credentials.

---

**15. How do you calculate the statistics?**

`ItemDAO.getStatistics()` uses one SQL query with `COUNT(*)` and
`SUM(CASE WHEN ... THEN 1 ELSE 0 END)` expressions. `COALESCE(..., 0)` makes an
empty table show zeros. The recovered count counts every recovered item
regardless of report type, while lost/found totals count by `report_type`, so a
recovered found item is counted in both the found total and the recovered
total.
