# Beginner Learning Guide

This guide explains the project in plain language so that you can understand and
defend every part of it. Every idea points to a real file and method.

---

## 1. Big picture - what happens when the program starts

1. `Main.main()` runs.
2. `DBConnection.checkConnection()` tries to open a connection to MySQL using
   the details in `.env`.
3. `ItemDAO.verifySchema()` checks that the `items` table exists.
4. `ItemService` is created and given the DAO.
5. `ConsoleMenu.run()` prints the banner and the menu in a loop.

```java
ItemDAO itemDao = new ItemDAO();
ItemService service = new ItemService(itemDao);
try (Scanner scanner = new Scanner(System.in)) {
    ConsoleMenu menu = new ConsoleMenu(service, scanner);
    menu.run();
}
```

(`src/main/java/com/campus/lostfound/Main.java`)

## 2. What each class does

| Class            | One-line job                                                    | File |
|------------------|-----------------------------------------------------------------|------|
| `Main`           | Starts the program after checking the database.                 | `Main.java` |
| `Item`           | Holds the data of one lost/found report.                        | `model/Item.java` |
| `Statistics`     | Holds the six numbers for the statistics screen.                | `model/Statistics.java` |
| `DBConnection`   | Opens JDBC connections and reads `.env`.                        | `util/DBConnection.java` |
| `ItemDAO`        | Runs all SQL (INSERT, SELECT, UPDATE, DELETE).                  | `dao/ItemDAO.java` |
| `ItemService`    | Applies rules (validation, existence checks, recovery rules).    | `service/ItemService.java` |
| `ConsoleMenu`    | Prints menus and tables, reads choices, calls the service.       | `console/ConsoleMenu.java` |
| `InputValidator` | Reads and validates typed input.                                | `console/InputValidator.java` |

## 3. Why is the `Item` class needed?

Without `Item`, the program would pass many separate variables around
(name, category, date, status, ...). That is hard to read and easy to mix up.
`Item` groups all the fields of one report into a single object, so methods can
take "an item" as one parameter:

```java
public int addItem(Item item) throws SQLException { ... }
```

`model/Item.java` is also the place that knows the allowed report types and
statuses through constants like `Item.TYPE_LOST`.

## 4. What does encapsulation mean in this project?

Encapsulation means the data inside an object is **private** and can only be
reached through methods. In `Item`:

```java
private String itemName;          // private: nobody outside can touch it

public String getItemName() {     // controlled read
    return itemName;
}

public void setItemName(String itemName) {  // controlled write
    this.itemName = itemName;
}
```

Why it matters: no other class can set `itemName` to `null` or to garbage by
accident, and if we ever need to validate or transform a value we only change
one place. The service layer uses these setters to normalise text (trim spaces,
store blanks as NULL):

```java
item.setItemName(requireText(item.getItemName(), "Item name"));
```

(`ItemService.validate`)

## 5. Why does the DAO class exist?

DAO stands for **Data Access Object**. `ItemDAO` is the only class that knows
SQL. If the console wrote SQL itself, the code would be tangled with menus and
impossible to test or change. Keeping all SQL in one place means:

* the SQL is easy to find and review,
* the rest of the program does not depend on the database,
* switching databases would touch only this file.

## 6. What is JDBC?

JDBC (Java Database Connectivity) is the standard Java way to talk to a
database. It provides:

* `DriverManager.getConnection(url, user, password)` to open a connection,
* `Connection`, `PreparedStatement` and `ResultSet` to run queries and read
  rows.

The MySQL driver jar (`mysql-connector-j`) is the "translator" that implements
JDBC for MySQL. It is declared once in `pom.xml`.

## 7. What is a `PreparedStatement`?

A `PreparedStatement` is a pre-compiled SQL statement with `?` placeholders. You
set each `?` with a method like `setString` or `setInt`. Example:

```java
String sql = "SELECT ... FROM items WHERE item_id = ?";
PreparedStatement statement = connection.prepareStatement(sql);
statement.setInt(1, itemId);
```

Advantages:

* **Security:** user input is never glued into the SQL text, so SQL injection
  is impossible. (`searchItems` even builds a dynamic query, but still uses `?`
  for every value.)
* **Correctness:** types and special characters are handled by JDBC.
* **Readability:** the SQL is separate from the values.

## 8. What is a primary key?

A primary key is a column (or set of columns) that uniquely identifies every
row. Here it is `item_id`. Because it is `AUTO_INCREMENT`, MySQL fills it in
automatically and we never have to invent IDs. When we insert a row we ask for
the generated key:

```java
PreparedStatement statement =
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
...
try (ResultSet keys = statement.getGeneratedKeys()) {
    if (keys.next()) { return keys.getInt(1); }
}
```

(`ItemDAO.addItem`)

## 9. Why MySQL?

* It is a free, widely used relational database that fits the course syllabus.
* It enforces data rules (NOT NULL, CHECK) at the database level.
* It stores data on disk, so records survive after the program closes.
* JDBC support is mature and well documented.

The program does **not** keep the items in a Java list or a text file; MySQL is
the single source of truth.

## 10. How each CRUD operation works

**Create** - the console builds an `Item`, the service validates it, and the DAO
runs an `INSERT`. MySQL assigns the ID, which the DAO returns.

**Read** - the DAO runs `SELECT`; `mapRow` turns each row into an `Item`; the
console prints them in a table.

**Update** - the console loads the current item, collects new values (leaving a
field blank keeps its old value), shows a summary, and on confirmation the DAO
runs an `UPDATE` that also refreshes `updated_at`.

**Delete** - the DAO runs `DELETE FROM items WHERE item_id = ?` only after the
user confirms.

**Recover** is a special update: it changes only `status` to `RECOVERED` and
requires the current status to be `ACTIVE`, so it can never run twice and never
touches `report_type`.

## 11. How the menu connects to the service and the DAO

```text
ConsoleMenu  --calls-->  ItemService  --calls-->  ItemDAO  --JDBC-->  MySQL
   (menu)                  (rules)                 (SQL)
```

For example, menu option 8 (mark recovered):

1. `ConsoleMenu.markRecovered()` asks for an ID and shows the record.
2. It calls `service.markAsRecovered(id)`.
3. `ItemService` checks the record exists and is not already recovered.
4. It calls `itemDao.markAsRecovered(id)`, which runs the `UPDATE`.
5. The boolean result travels back up and the console prints the message.

## 12. How to explain the project to your professor (short script)

> "It is a console Java program with four layers: the console collects input,
> the service applies the rules, the DAO runs SQL through JDBC, and MySQL stores
> the data. The `Item` class is the data model and is fully encapsulated. Lost
> and found are the same record with a `report_type`; recovery only changes the
> `status`, so a recovered found item is still a found report. Every query uses
> `PreparedStatement`, connections use try-with-resources, and the database only
> has one table because the problem has one entity. I tested all CRUD features,
> validation, search, pagination and database-error handling against a real
> MySQL server."

## 13. Getting the project running (student checklist)

1. Install JDK 17, MySQL 8 and Maven.
2. Run `database/schema.sql` as root.
3. Create the `campus_app` user and grant it access to `campus_lost_found`.
4. `Copy-Item .env.example .env` and put the real password in `.env`.
5. `mvn clean package`.
6. `java -jar target/campus-lost-and-found-1.0.0.jar`.
7. Try each menu option, then `mvn test`-style check the database with
   `mysql -u campus_app -p campus_lost_found -e "SELECT * FROM items;"`.

## 14. Words you should be able to define

* **Class / Object** - blueprint / a thing built from it.
* **Encapsulation** - private fields + public methods.
* **Constructor** - method that builds an object.
* **DAO** - class that owns all database access.
* **JDBC** - Java's API for databases.
* **PreparedStatement** - a parameterised, injection-safe SQL statement.
* **Primary key** - the column that uniquely identifies a row.
* **Persistence** - data survives after the program stops (we get this from
  MySQL).
* **CRUD** - Create, Read, Update, Delete.
