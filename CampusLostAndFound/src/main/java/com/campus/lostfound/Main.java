package com.campus.lostfound;

import com.campus.lostfound.console.ConsoleMenu;
import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.DBConnection;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * Main is the entry point of the application.
 *
 * Start-up order:
 *   1. check that MySQL is reachable with the configured credentials,
 *   2. check that the items table exists (database/schema.sql),
 *   3. build the service layer,
 *   4. start the console menu.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Connecting to MySQL...");

        try {
            DBConnection.checkConnection();
        } catch (SQLException e) {
            printConnectionFailure(e);
            return;
        }

        ItemDAO itemDao = new ItemDAO();

        try {
            itemDao.verifySchema();
        } catch (SQLException e) {
            System.out.println();
            System.out.println("The database structure could not be verified.");
            System.out.println("Details: " + e.getMessage());
            return;
        }

        System.out.println("Database connection established. "
                + "(" + DBConnection.describeConfiguration() + ")");
        System.out.println();

        ItemService service = new ItemService(itemDao);

        // The Scanner is closed when the menu ends, which also closes System.in.
        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleMenu menu = new ConsoleMenu(service, scanner);
            menu.run();
        }

        System.out.println("Application finished.");
    }

    /**
     * Explains a connection problem in plain language.
     * The JDBC message is printed because it never contains the password
     * value itself (MySQL only reports "using password: YES/NO").
     */
    private static void printConnectionFailure(SQLException e) {
        System.out.println();
        System.out.println("Unable to connect to the database. Please check your MySQL configuration.");
        System.out.println("Configuration: " + DBConnection.describeConfiguration());
        System.out.println("Reason: " + e.getMessage());

        if (e.getMessage() != null && e.getMessage().contains("No suitable driver")) {
            System.out.println();
            System.out.println("The MySQL JDBC driver is missing from the classpath.");
            System.out.println("Run the program with 'mvn compile exec:java' or with the");
            System.out.println("packaged jar created by 'mvn package':");
            System.out.println("    java -jar target/campus-lost-and-found-1.0.0.jar");
            return;
        }

        System.out.println();
        System.out.println("How to fix this:");
        System.out.println("  1. Make sure MySQL is running (on Windows: services.msc -> MySQL80).");
        System.out.println("  2. Copy .env.example to .env and set DB_USER and DB_PASSWORD.");
        System.out.println("  3. Run database/schema.sql once as an administrator to create the table.");

        if ("true".equalsIgnoreCase(System.getenv("APP_DEBUG"))) {
            System.out.println();
            e.printStackTrace(System.out);
        }
    }
}
