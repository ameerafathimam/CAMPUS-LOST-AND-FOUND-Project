# Java OOP Concepts Used in This Project

This document points to the **actual places in the source code** where each
Object-Oriented Programming concept appears, so the project can be explained
line by line during an evaluation. Nothing here is theoretical - every example
exists in the code.

---

## 1. Class and Object

* **Class** = a blueprint. **Object** = one thing built from that blueprint.

`model/Item.java` defines the `Item` class. Every row read from MySQL becomes
one `Item` object:

```java
Item item = new Item();
item.setItemId(resultSet.getInt("item_id"));
item.setItemName(resultSet.getString("item_name"));
```

(`dao/ItemDAO.java`, `mapRow` method.)

The console also creates `Item` objects while reading user input:

```java
Item item = new Item(name, category, description, reportType,
        location, reportDate, contactInfo, Item.STATUS_ACTIVE);
```

(`console/ConsoleMenu.java`, `reportItem` method.)

## 2. Encapsulation

All fields of `Item` are **private**. Other classes cannot change them
directly; they must use the public getters and setters, which keeps the data
consistent:

```java
private String itemName;

public String getItemName() {
    return itemName;
}

public void setItemName(String itemName) {
    this.itemName = itemName;
}
```

`model/Item.java`.

Encapsulation is also why the console never touches SQL: it can only call
`ItemService` methods, and the service protects its own rules.

## 3. Constructors

`Item` has two constructors:

* `public Item()` - an empty object, used while collecting user input and while
  reading rows.
* `public Item(String itemName, ... , String status)` - fills a new report in
  one step.

`model/Item.java`. `ItemService` also has a constructor that receives its
dependency:

```java
public ItemService(ItemDAO itemDao) {
    this.itemDao = itemDao;
}
```

## 4. Abstraction

The `Main` class and the console do not know how data is stored. They only call
service methods:

```java
int itemId = isLost ? service.reportLostItem(item) : service.registerFoundItem(item);
```

(`console/ConsoleMenu.java`.) The SQL, the JDBC connection, the `.env` file and
even the database type are hidden behind `ItemService` and `ItemDAO`. If the
project later moved from MySQL to another database, `Main` and `ConsoleMenu`
would not change at all.

## 5. Methods and Single Responsibility

Each class has one clear job:

| Class          | Responsibility                          | Example method          |
|----------------|-----------------------------------------|-------------------------|
| `Item`         | hold data                               | `getReportDate()`       |
| `Statistics`   | hold statistics numbers                 | `getRecoveredItems()`   |
| `DBConnection` | open MySQL connections                  | `getConnection()`       |
| `ItemDAO`      | run SQL                                 | `searchItems(...)`      |
| `ItemService`  | apply business rules                    | `markAsRecovered(...)`  |
| `ConsoleMenu`  | print menus / tables and read choices   | `displayItems(...)`     |
| `InputValidator`| validate typed input                   | `requireDate(...)`      |
| `Main`         | start the program                       | `main(...)`             |

## 6. Static members and constants

Constants are defined once and reused everywhere, so the allowed values exist
in a single place:

```java
public static final String TYPE_LOST  = "LOST";
public static final String TYPE_FOUND = "FOUND";
public static final String STATUS_ACTIVE    = "ACTIVE";
public static final String STATUS_RECOVERED = "RECOVERED";
```

`model/Item.java`. These values match the `CHECK` constraints in the database.

`DBConnection` is a utility class: it is never instantiated (its constructor is
private) and its methods are static, e.g. `DBConnection.getConnection()`.

Helper methods such as `Item.normalizeReportType(...)` and
`InputValidator.parseDate(...)` are static because they do not need object
state.

## 7. Immutability (a small, useful case)

`Statistics` stores its numbers in `final` fields set once by the constructor.
Once created, a `Statistics` object cannot be changed:

```java
private final int totalItems;

public int getTotalItems() {
    return totalItems;
}
```

`model/Statistics.java`.

## 8. Method overloading (constructors) and overloading of behaviour

`Item` has two constructors with different parameter lists - a simple example
of **overloading**. The class also **overrides** the `toString()` method
inherited from `Object` to print a readable record:

```java
@Override
public String toString() {
    return "Item ID   : " + itemId + "\n"
         + "Name      : " + itemName + "\n" ...
}
```

`model/Item.java`.

## 9. Encapsulation of rules (defensive design)

`ItemService.validate(...)` is the single place where field rules are enforced:

```java
if (item.getReportDate().isAfter(LocalDate.now())) {
    throw new IllegalArgumentException("Report date cannot be in the future.");
}
```

This method is called before every insert and update, so the DAO only ever
receives data that already satisfies the business rules.

## 10. Exception handling as part of good design

Checked `SQLException`s are allowed to travel up from the DAO to the console,
which catches them in one place and prints a friendly message:

```java
} catch (IllegalArgumentException e) {
    System.out.println("\n" + e.getMessage());
} catch (SQLException e) {
    handleDatabaseError(e);
}
```

`console/ConsoleMenu.java`, `handleChoice` method.

---

### What was deliberately NOT used

To keep the assignment honest and readable, the project does **not** force
inheritance, interfaces, generics-heavy code or design patterns that the
problem does not need. Inheritance and interfaces are useful when several
classes share behaviour or when multiple implementations are expected; here a
small, clear set of classes with a single responsibility is more maintainable
and easier to explain.
