package com.campus.lostfound.console;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.Statistics;
import com.campus.lostfound.service.ItemService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * ConsoleMenu is the user interface of the application.
 *
 * It prints the menus and tables, collects input through InputValidator
 * and delegates every database operation to ItemService. It never writes
 * SQL itself.
 */
public class ConsoleMenu {

    /** How many table rows are printed before asking for the next page. */
    private static final int PAGE_SIZE = 10;

    private static final String DIVIDER = "----------------------------------------------------";

    private final ItemService service;
    private final InputValidator input;
    private boolean running = true;

    public ConsoleMenu(ItemService service, Scanner scanner) {
        this.service = service;
        this.input = new InputValidator(scanner);
    }

    /** Shows the menu again and again until the user selects Exit. */
    public void run() {
        printBanner();
        try {
            while (running) {
                printMainMenu();
                int choice = input.readMenuChoice("Enter your choice: ", 1, 11);
                handleChoice(choice);
            }
        } catch (NoSuchElementException e) {
            // The input stream ended (for example when a piped test file runs out).
            System.out.println("\nNo more input received. Closing the application.");
        }
    }

    // ------------------------------------------------------------------
    // Main menu
    // ------------------------------------------------------------------

    private void handleChoice(int choice) {
        try {
            switch (choice) {
                case 1 -> reportItem(Item.TYPE_LOST);
                case 2 -> reportItem(Item.TYPE_FOUND);
                case 3 -> viewAllItems();
                case 4 -> searchMenu();
                case 5 -> viewItemsOfType(Item.TYPE_LOST);
                case 6 -> viewItemsOfType(Item.TYPE_FOUND);
                case 7 -> updateItem();
                case 8 -> markRecovered();
                case 9 -> deleteItem();
                case 10 -> viewStatistics();
                case 11 -> exit();
                default -> System.out.println("Invalid choice. Please enter a number from the menu.");
            }
        } catch (IllegalArgumentException e) {
            // Business rule violations carry a message written for the user.
            System.out.println("\n" + e.getMessage());
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private void printBanner() {
        System.out.println("====================================================");
        System.out.println("CAMPUS LOST AND FOUND MANAGEMENT SYSTEM");
        System.out.println("=======================================");
        System.out.println();
        System.out.println("  A Simple Campus Item Recovery System");
        System.out.println();
        System.out.println("====================================================");
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println(" 1. Report a Lost Item");
        System.out.println(" 2. Register a Found Item");
        System.out.println(" 3. View All Items");
        System.out.println(" 4. Search Items");
        System.out.println(" 5. View Lost Items");
        System.out.println(" 6. View Found Items");
        System.out.println(" 7. Update Item Details");
        System.out.println(" 8. Mark Item as Recovered");
        System.out.println(" 9. Delete Item Record");
        System.out.println("10. View Statistics");
        System.out.println("11. Exit");
        System.out.println(DIVIDER);
    }

    // ------------------------------------------------------------------
    // 1 and 2: create reports (one shared implementation)
    // ------------------------------------------------------------------

    /**
     * Handles both "Report a Lost Item" and "Register a Found Item".
     * The only differences are the wording of the prompts and the report
     * type stored in the database, so one method covers both features.
     */
    private void reportItem(String reportType) throws SQLException {
        boolean isLost = Item.TYPE_LOST.equals(reportType);
        System.out.println();
        System.out.println(isLost ? "-- REPORT A LOST ITEM --" : "-- REGISTER A FOUND ITEM --");

        String name = input.requireNonEmpty("Item name               : ",
                "Item name cannot be empty.");
        String category = input.requireNonEmpty("Category                : ",
                "Category cannot be empty. Examples: Documents, Electronics, Keys, Bottles.");
        String description = input.readOptional("Description (optional)  : ");
        String location = input.requireNonEmpty(
                isLost ? "Last seen location      : " : "Found location          : ",
                "Location cannot be empty.");
        LocalDate reportDate = input.requireDate(
                isLost ? "Date lost (yyyy-mm-dd)   : " : "Date found (yyyy-mm-dd)  : ",
                "Invalid date. Please use the format yyyy-mm-dd (example: 2026-10-05).");
        String contactInfo = input.readOptional("Contact info (optional) : ");

        Item item = new Item(name, category, description, reportType,
                location, reportDate, contactInfo, Item.STATUS_ACTIVE);

        int itemId = isLost ? service.reportLostItem(item) : service.registerFoundItem(item);
        System.out.println("\nItem added successfully. Item ID: " + itemId);
    }

    // ------------------------------------------------------------------
    // 3, 5 and 6: views
    // ------------------------------------------------------------------

    private void viewAllItems() throws SQLException {
        System.out.println();
        System.out.println("-- ALL ITEMS --");
        displayItems(service.getAllItems(),
                "No item records found. Report a lost item or register a found one to get started.");
    }

    private void viewItemsOfType(String reportType) throws SQLException {
        boolean isLost = Item.TYPE_LOST.equals(reportType);
        System.out.println();
        System.out.println(isLost ? "-- LOST ITEMS --" : "-- FOUND ITEMS --");

        List<Item> items = isLost ? service.getLostItems() : service.getFoundItems();
        displayItems(items, isLost
                ? "No lost items have been registered."
                : "No found items have been registered.");
    }

    // ------------------------------------------------------------------
    // 4: search
    // ------------------------------------------------------------------

    private void searchMenu() throws SQLException {
        while (true) {
            System.out.println();
            System.out.println(DIVIDER);
            System.out.println(" SEARCH ITEMS");
            System.out.println(DIVIDER);
            System.out.println("1. Search by Item ID");
            System.out.println("2. Search by Item name (partial match)");
            System.out.println("3. Search by Category");
            System.out.println("4. Search by Location");
            System.out.println("5. Search by Report type (LOST / FOUND)");
            System.out.println("6. Search by Status (ACTIVE / RECOVERED)");
            System.out.println("0. Back to Main Menu");
            System.out.println(DIVIDER);

            int choice = input.readMenuChoice("Enter your choice: ", 0, 6);
            if (choice == 0) {
                return;
            }
            runSearch(choice);
        }
    }

    private void runSearch(int choice) throws SQLException {
        List<Item> results;

        switch (choice) {
            case 1 -> {
                int itemId = input.readItemId("Enter Item ID: ");
                results = service.searchItems(itemId, null, null, null, null, null);
            }
            case 2 -> {
                String name = input.requireNonEmpty("Enter item name (partial match is fine): ",
                        "Search text cannot be empty.");
                results = service.searchItems(null, name, null, null, null, null);
            }
            case 3 -> {
                String category = input.requireNonEmpty("Enter category: ",
                        "Search text cannot be empty.");
                results = service.searchItems(null, null, category, null, null, null);
            }
            case 4 -> {
                String location = input.requireNonEmpty("Enter location: ",
                        "Search text cannot be empty.");
                results = service.searchItems(null, null, null, location, null, null);
            }
            case 5 -> {
                String reportType = input.requireReportType("Enter report type (LOST or FOUND): ");
                results = service.searchItems(null, null, null, null, reportType, null);
            }
            case 6 -> {
                String status = input.requireStatus("Enter status (ACTIVE or RECOVERED): ");
                results = service.searchItems(null, null, null, null, null, status);
            }
            default -> {
                return;
            }
        }

        System.out.println();
        displayItems(results, "No matching records found.");
    }

    // ------------------------------------------------------------------
    // 7: update
    // ------------------------------------------------------------------

    private void updateItem() throws SQLException {
        System.out.println();
        System.out.println("-- UPDATE ITEM DETAILS --");

        int itemId = input.readItemId("Enter Item ID to update: ");
        Item current = service.getItemById(itemId);
        if (current == null) {
            System.out.println("Item not found. Please check the item ID.");
            return;
        }

        System.out.println("\nCurrent record:");
        printDetails(current);
        System.out.println("\nEnter new values. Press Enter to keep the current value.");

        Item proposed = new Item();
        proposed.setItemId(current.getItemId());
        proposed.setItemName(keepIfBlank(
                input.readOptional("\nItem name [" + valueOrDash(current.getItemName()) + "]: "),
                current.getItemName()));
        proposed.setCategory(keepIfBlank(
                input.readOptional("Category [" + valueOrDash(current.getCategory()) + "]: "),
                current.getCategory()));
        proposed.setDescription(keepIfBlank(
                input.readOptional("Description [" + valueOrDash(current.getDescription()) + "]: "),
                current.getDescription()));
        proposed.setLocation(keepIfBlank(
                input.readOptional("Location [" + valueOrDash(current.getLocation()) + "]: "),
                current.getLocation()));
        proposed.setContactInfo(keepIfBlank(
                input.readOptional("Contact info [" + valueOrDash(current.getContactInfo()) + "]: "),
                current.getContactInfo()));
        proposed.setReportType(readReportTypeOrKeep(current));
        proposed.setReportDate(readDateOrKeep(current));
        // Status is edited only through "Mark Item as Recovered", so the
        // existing value is preserved here.
        proposed.setStatus(current.getStatus());

        List<String> changes = describeChanges(current, proposed);
        if (changes.isEmpty()) {
            System.out.println("\nNo changes were made.");
            return;
        }

        System.out.println("\nProposed changes:");
        for (String change : changes) {
            System.out.println("  - " + change);
        }

        if (!input.confirm("\nSave these changes? (Y/N): ")) {
            System.out.println("Operation cancelled.");
            return;
        }

        if (service.updateItem(proposed)) {
            System.out.println("Item updated successfully. Item ID: " + itemId);
        } else {
            System.out.println("The update could not be completed. No changes were saved.");
        }
    }

    private String readReportTypeOrKeep(Item current) {
        String entered = input.readOptional(
                "Report type [" + current.getReportType() + "] (LOST / FOUND): ");
        if (entered.isBlank()) {
            return current.getReportType();
        }
        String normalized = Item.normalizeReportType(entered);
        if (normalized == null) {
            System.out.println("Invalid report type. Keeping the current value: "
                    + current.getReportType());
            return current.getReportType();
        }
        return normalized;
    }

    private LocalDate readDateOrKeep(Item current) {
        String entered = input.readOptional(
                "Report date [" + current.getReportDate() + "] (yyyy-mm-dd): ");
        if (entered.isBlank()) {
            return current.getReportDate();
        }
        LocalDate parsed = InputValidator.parseDate(entered);
        if (parsed == null) {
            System.out.println("Invalid date. Keeping the current value: " + current.getReportDate());
            return current.getReportDate();
        }
        return parsed;
    }

    /** Compares the loaded record with the proposed one, field by field. */
    private List<String> describeChanges(Item current, Item proposed) {
        List<String> changes = new ArrayList<>();
        addChange(changes, "Item name", current.getItemName(), proposed.getItemName());
        addChange(changes, "Category", current.getCategory(), proposed.getCategory());
        addChange(changes, "Description", current.getDescription(), proposed.getDescription());
        addChange(changes, "Report type", current.getReportType(), proposed.getReportType());
        addChange(changes, "Location", current.getLocation(), proposed.getLocation());
        addChange(changes, "Report date",
                String.valueOf(current.getReportDate()), String.valueOf(proposed.getReportDate()));
        addChange(changes, "Contact info", current.getContactInfo(), proposed.getContactInfo());
        return changes;
    }

    private void addChange(List<String> changes, String field, String before, String after) {
        String oldValue = before == null ? "-" : before;
        String newValue = after == null ? "-" : after;
        if (!oldValue.equals(newValue)) {
            changes.add(field + ": " + oldValue + " -> " + newValue);
        }
    }

    // ------------------------------------------------------------------
    // 8: recover
    // ------------------------------------------------------------------

    private void markRecovered() throws SQLException {
        System.out.println();
        System.out.println("-- MARK ITEM AS RECOVERED --");

        int itemId = input.readItemId("Enter Item ID: ");
        Item item = service.getItemById(itemId);
        if (item == null) {
            System.out.println("Item not found. Please check the item ID.");
            return;
        }

        System.out.println("\nCurrent record:");
        printDetails(item);

        if (Item.STATUS_RECOVERED.equals(item.getStatus())) {
            System.out.println("\nItem ID " + itemId + " has already been recovered. No changes were made.");
            return;
        }

        if (!input.confirm("\nMark this item as recovered? (Y/N): ")) {
            System.out.println("Operation cancelled.");
            return;
        }

        if (service.markAsRecovered(itemId)) {
            System.out.println("Item marked as recovered successfully.");
            System.out.println("(The report type stays " + item.getReportType()
                    + ", only the status changed.)");
        } else {
            System.out.println("The item could not be updated. No changes were made.");
        }
    }

    // ------------------------------------------------------------------
    // 9: delete
    // ------------------------------------------------------------------

    private void deleteItem() throws SQLException {
        System.out.println();
        System.out.println("-- DELETE ITEM RECORD --");

        int itemId = input.readItemId("Enter Item ID to delete: ");
        Item item = service.getItemById(itemId);
        if (item == null) {
            System.out.println("Item not found. Please check the item ID.");
            return;
        }

        System.out.println("\nRecord to be deleted:");
        printDetails(item);

        if (!input.confirm("\nDelete this record permanently? (Y/N): ")) {
            System.out.println("Operation cancelled.");
            return;
        }

        if (service.deleteItem(itemId)) {
            System.out.println("Item record deleted successfully. Item ID: " + itemId);
        } else {
            System.out.println("The record could not be deleted. No changes were made.");
        }
    }

    // ------------------------------------------------------------------
    // 10: statistics
    // ------------------------------------------------------------------

    private void viewStatistics() throws SQLException {
        Statistics stats = service.getStatistics();

        System.out.println();
        System.out.println("-- STATISTICS --");
        System.out.println(DIVIDER);
        System.out.printf("Total registered items : %d%n", stats.getTotalItems());
        System.out.printf("Total lost-item reports: %d%n", stats.getLostItems());
        System.out.printf("Total found-item reports: %d%n", stats.getFoundItems());
        System.out.printf("Active lost items      : %d%n", stats.getActiveLostItems());
        System.out.printf("Active found items     : %d%n", stats.getActiveFoundItems());
        System.out.printf("Total recovered items  : %d%n", stats.getRecoveredItems());
        System.out.println(DIVIDER);
        System.out.println("(Lost/found totals include recovered items; every");
        System.out.println(" recovered item is counted once in its own report type.");
        System.out.println(" Recovered items also add to the recovered total.)");
    }

    // ------------------------------------------------------------------
    // 11: exit
    // ------------------------------------------------------------------

    private void exit() {
        running = false;
        System.out.println();
        System.out.println("Thank you for using the Campus Lost and Found Management System.");
        System.out.println("Goodbye!");
    }

    // ------------------------------------------------------------------
    // Display helpers
    // ------------------------------------------------------------------

    /** Prints a table of items, 10 rows at a time when there are many. */
    private void displayItems(List<Item> items, String emptyMessage) {
        if (items == null || items.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }

        int index = 0;
        while (index < items.size()) {
            printTableHeader();
            int end = Math.min(index + PAGE_SIZE, items.size());
            for (; index < end; index++) {
                printItemRow(items.get(index));
            }

            if (index < items.size()) {
                System.out.printf("(%d of %d records shown)%n", index, items.size());
                String more = input.readOptional("Press Enter for next page, or type q to stop: ");
                if ("q".equalsIgnoreCase(more)) {
                    break;
                }
            }
        }
        System.out.printf("Total records: %d%n", items.size());
    }

    private void printTableHeader() {
        System.out.printf("%-5s %-22s %-12s %-6s %-22s %-10s %-10s%n",
                "ID", "NAME", "CATEGORY", "TYPE", "LOCATION", "DATE", "STATUS");
        System.out.println("----- ---------------------- ------------ ------ "
                + "---------------------- ---------- ----------");
    }

    private void printItemRow(Item item) {
        System.out.printf("%-5s %-22s %-12s %-6s %-22s %-10s %-10s%n",
                item.getItemId(),
                truncate(item.getItemName(), 22),
                truncate(item.getCategory(), 12),
                item.getReportType(),
                truncate(item.getLocation(), 22),
                item.getReportDate(),
                item.getStatus());
    }

    /** Prints one record in the detailed, key/value form. */
    private void printDetails(Item item) {
        System.out.println(DIVIDER);
        for (String line : item.toString().split("\\R")) {
            System.out.println("  " + line);
        }
        System.out.println(DIVIDER);
    }

    private String truncate(String value, int width) {
        if (value == null) {
            return "-";
        }
        if (value.length() <= width) {
            return value;
        }
        return value.substring(0, width - 3) + "...";
    }

    private String keepIfBlank(String entered, String current) {
        return entered.isBlank() ? current : entered;
    }

    private String valueOrDash(String value) {
        return value == null ? "-" : value;
    }

    /** Prints a friendly database error; technical details only in debug mode. */
    private void handleDatabaseError(SQLException e) {
        System.out.println("\nThe database operation could not be completed.");
        if (debugEnabled()) {
            System.out.println("Details: " + e.getMessage());
            e.printStackTrace(System.out);
        } else {
            System.out.println("Check that MySQL is running and that the .env file is correct.");
            System.out.println("(Set the environment variable APP_DEBUG=true to see technical details.)");
        }
    }

    private boolean debugEnabled() {
        return "true".equalsIgnoreCase(System.getenv("APP_DEBUG"));
    }
}
