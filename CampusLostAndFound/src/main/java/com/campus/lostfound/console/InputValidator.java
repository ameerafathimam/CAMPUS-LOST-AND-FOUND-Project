package com.campus.lostfound.console;

import com.campus.lostfound.model.Item;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

/**
 * Reads and validates everything the user types.
 *
 * IMPORTANT: this class only ever uses Scanner.nextLine() and then parses
 * the returned String. Mixing nextInt() with nextLine() is the classic
 * Scanner bug that makes menus skip input, so it is avoided on purpose.
 */
public class InputValidator {

    /** Accepts both 2026-10-05 and 2026-1-5 style dates. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-M-d").withResolverStyle(ResolverStyle.STRICT);

    private final Scanner scanner;

    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Reads a menu number inside the allowed range.
     * Repeats until the user types a valid number, so the program never
     * crashes on text such as "abc" or a number outside the menu.
     */
    public int readMenuChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(raw);
                if (choice >= min && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
                // Fall through to the error message below.
            }
            System.out.println("Invalid choice. Please enter a number from " + min + " to " + max + ".");
        }
    }

    /** Reads a required (non-empty) line of text. */
    public String requireNonEmpty(String prompt, String errorMessage) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println(errorMessage);
        }
    }

    /** Reads an optional line of text. Returns "" when the user presses Enter. */
    public String readOptional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /** Reads a required date in yyyy-mm-dd format. */
    public LocalDate requireDate(String prompt, String errorMessage) {
        while (true) {
            System.out.print(prompt);
            LocalDate date = parseDate(scanner.nextLine());
            if (date != null) {
                return date;
            }
            System.out.println(errorMessage);
        }
    }

    /**
     * Parses a date manually instead of relying on LocalDate.parse(),
     * because the default ISO parser rejects short months and days such
     * as 2026-1-5. Returns null when the text is not a real date.
     */
    public static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Reads a positive item ID such as 3. Rejects text, zero and negatives. */
    public int readItemId(String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                int itemId = Integer.parseInt(raw);
                if (itemId > 0) {
                    return itemId;
                }
            } catch (NumberFormatException ignored) {
                // Fall through to the error message below.
            }
            System.out.println("Invalid item ID. Please enter a positive number (example: 3).");
        }
    }

    /** Reads a report type, accepting any capitalisation of LOST / FOUND. */
    public String requireReportType(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = Item.normalizeReportType(scanner.nextLine());
            if (value != null) {
                return value;
            }
            System.out.println("Invalid report type. Please enter LOST or FOUND.");
        }
    }

    /** Reads a status, accepting any capitalisation of ACTIVE / RECOVERED. */
    public String requireStatus(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = Item.normalizeStatus(scanner.nextLine());
            if (value != null) {
                return value;
            }
            System.out.println("Invalid status. Please enter ACTIVE or RECOVERED.");
        }
    }

    /**
     * Asks a yes/no question. Anything other than Y/YES/N/NO is rejected
     * and asked again, so a stray key press can never delete a record.
     */
    public boolean confirm(String prompt) {
        while (true) {
            System.out.print(prompt);
            String answer = scanner.nextLine().trim().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            System.out.println("Please answer Y or N.");
        }
    }
}
