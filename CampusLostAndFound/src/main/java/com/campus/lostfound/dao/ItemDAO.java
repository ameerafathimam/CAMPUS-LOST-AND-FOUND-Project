package com.campus.lostfound.dao;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.Statistics;
import com.campus.lostfound.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemDAO (Data Access Object) is the ONLY class that talks to MySQL.
 *
 * Every method builds an SQL statement, executes it with a
 * PreparedStatement and converts the ResultSet into Item objects.
 * The console and the service layer never write SQL themselves.
 *
 * All resources are closed automatically by try-with-resources.
 */
public class ItemDAO {

    private static final String COLUMNS =
            "item_id, item_name, category, description, report_type, "
            + "location, report_date, contact_info, status, created_at, updated_at";

    /**
     * Inserts a new report and returns the automatically generated item ID.
     * The generated key is also written back into the Item object.
     */
    public int addItem(Item item) throws SQLException {
        String sql = "INSERT INTO items (item_name, category, description, report_type, "
                + "location, report_date, contact_info, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, item.getItemName());
            statement.setString(2, item.getCategory());
            statement.setString(3, item.getDescription());
            statement.setString(4, item.getReportType());
            statement.setString(5, item.getLocation());
            statement.setDate(6, Date.valueOf(item.getReportDate()));
            statement.setString(7, item.getContactInfo());
            statement.setString(8, item.getStatus());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Insert failed: no rows were affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    item.setItemId(generatedId);
                    return generatedId;
                }
                throw new SQLException("Insert failed: the generated item ID was not returned.");
            }
        }
    }

    /** Returns every record, newest first. */
    public List<Item> getAllItems() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM items ORDER BY item_id DESC";
        return executeQuery(sql);
    }

    /**
     * Finds one record by its primary key.
     * Returns null when the ID does not exist.
     */
    public Item getItemById(int itemId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM items WHERE item_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, itemId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * Builds a search query dynamically. Every parameter that is not null
     * (and not blank) becomes one more "AND ... = ?" or "AND ... LIKE ?"
     * clause. Because placeholders are used everywhere, user input is never
     * concatenated into SQL, which prevents SQL injection.
     *
     * @param itemId     exact ID, or null to ignore
     * @param name       partial item name, or null/blank to ignore
     * @param category   exact category, or null/blank to ignore
     * @param location   partial location, or null/blank to ignore
     * @param reportType LOST or FOUND, or null/blank to ignore
     * @param status     ACTIVE or RECOVERED, or null/blank to ignore
     */
    public List<Item> searchItems(Integer itemId, String name, String category,
                                  String location, String reportType, String status) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM items WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();

        if (itemId != null) {
            sql.append(" AND item_id = ?");
            parameters.add(itemId);
        }
        if (isPresent(name)) {
            sql.append(" AND item_name LIKE ?");
            parameters.add("%" + name.trim() + "%");
        }
        if (isPresent(category)) {
            sql.append(" AND category LIKE ?");
            parameters.add("%" + category.trim() + "%");
        }
        if (isPresent(location)) {
            sql.append(" AND location LIKE ?");
            parameters.add("%" + location.trim() + "%");
        }
        if (isPresent(reportType)) {
            sql.append(" AND report_type = ?");
            parameters.add(reportType.trim());
        }
        if (isPresent(status)) {
            sql.append(" AND status = ?");
            parameters.add(status.trim());
        }

        sql.append(" ORDER BY item_id DESC");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapRows(resultSet);
            }
        }
    }

    /** Returns only LOST or only FOUND reports, depending on the parameter. */
    public List<Item> getItemsByReportType(String reportType) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM items WHERE report_type = ? ORDER BY item_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, reportType);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapRows(resultSet);
            }
        }
    }

    /**
     * Updates every editable field of an existing record.
     * updated_at is refreshed by the database because the SQL sets it
     * explicitly to CURRENT_TIMESTAMP.
     */
    public boolean updateItem(Item item) throws SQLException {
        String sql = "UPDATE items SET item_name = ?, category = ?, description = ?, "
                + "report_type = ?, location = ?, report_date = ?, contact_info = ?, "
                + "updated_at = CURRENT_TIMESTAMP WHERE item_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, item.getItemName());
            statement.setString(2, item.getCategory());
            statement.setString(3, item.getDescription());
            statement.setString(4, item.getReportType());
            statement.setString(5, item.getLocation());
            statement.setDate(6, Date.valueOf(item.getReportDate()));
            statement.setString(7, item.getContactInfo());
            statement.setInt(8, item.getItemId());

            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Changes status from ACTIVE to RECOVERED.
     *
     * The WHERE clause also requires status = 'ACTIVE', so the same record
     * can never be recovered twice: the second call simply matches 0 rows.
     * report_type is NOT part of this statement, which is exactly why a
     * recovered item keeps its original LOST or FOUND label.
     */
    public boolean markAsRecovered(int itemId) throws SQLException {
        String sql = "UPDATE items SET status = 'RECOVERED', updated_at = CURRENT_TIMESTAMP "
                + "WHERE item_id = ? AND status = 'ACTIVE'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, itemId);
            return statement.executeUpdate() > 0;
        }
    }

    /** Deletes one record. Returns false when the ID does not exist. */
    public boolean deleteItem(int itemId) throws SQLException {
        String sql = "DELETE FROM items WHERE item_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, itemId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Calculates all statistics with a single query.
     *
     * CASE expressions behave like counters: every matching row adds 1,
     * every non-matching row adds 0. COALESCE turns the NULL produced by
     * SUM() over an empty table into 0, so an empty database still shows
     * a readable statistics screen instead of null values.
     */
    public Statistics getStatistics() throws SQLException {
        String sql = "SELECT "
                + "COUNT(*) AS total, "
                + "COALESCE(SUM(CASE WHEN report_type = 'LOST' THEN 1 ELSE 0 END), 0) AS lost_total, "
                + "COALESCE(SUM(CASE WHEN report_type = 'FOUND' THEN 1 ELSE 0 END), 0) AS found_total, "
                + "COALESCE(SUM(CASE WHEN report_type = 'LOST' AND status = 'ACTIVE' THEN 1 ELSE 0 END), 0) AS active_lost, "
                + "COALESCE(SUM(CASE WHEN report_type = 'FOUND' AND status = 'ACTIVE' THEN 1 ELSE 0 END), 0) AS active_found, "
                + "COALESCE(SUM(CASE WHEN status = 'RECOVERED' THEN 1 ELSE 0 END), 0) AS recovered "
                + "FROM items";

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                return new Statistics(
                        resultSet.getInt("total"),
                        resultSet.getInt("lost_total"),
                        resultSet.getInt("found_total"),
                        resultSet.getInt("active_lost"),
                        resultSet.getInt("active_found"),
                        resultSet.getInt("recovered"));
            }
            return new Statistics(0, 0, 0, 0, 0, 0);
        }
    }

    /**
     * Checks that the items table exists. Called once at startup so the
     * user gets a clear instruction to run database/schema.sql instead of
     * a raw "table doesn't exist" error later.
     */
    public void verifySchema() throws SQLException {
        String sql = "SELECT item_id FROM items LIMIT 1";
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            // Opening the result set is enough: the table exists.
        } catch (SQLException e) {
            if (e.getErrorCode() == 1146) {
                throw new SQLException("Table 'items' does not exist. "
                        + "Please run database/schema.sql once to create the database structure.");
            }
            throw e;
        }
    }

    /** Executes a simple SELECT that takes no parameters. */
    private List<Item> executeQuery(String sql) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return mapRows(resultSet);
        }
    }

    /** Converts every remaining ResultSet row into an Item object. */
    private List<Item> mapRows(ResultSet resultSet) throws SQLException {
        List<Item> items = new ArrayList<>();
        while (resultSet.next()) {
            items.add(mapRow(resultSet));
        }
        return items;
    }

    /** Converts one ResultSet row into an Item object. */
    private Item mapRow(ResultSet resultSet) throws SQLException {
        Item item = new Item();
        item.setItemId(resultSet.getInt("item_id"));
        item.setItemName(resultSet.getString("item_name"));
        item.setCategory(resultSet.getString("category"));
        item.setDescription(resultSet.getString("description"));
        item.setReportType(resultSet.getString("report_type"));
        item.setLocation(resultSet.getString("location"));

        Date reportDate = resultSet.getDate("report_date");
        item.setReportDate(reportDate != null ? reportDate.toLocalDate() : null);

        item.setContactInfo(resultSet.getString("contact_info"));
        item.setStatus(resultSet.getString("status"));

        java.sql.Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }
        java.sql.Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            item.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return item;
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
