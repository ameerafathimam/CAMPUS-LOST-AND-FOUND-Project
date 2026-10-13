package com.campus.lostfound.service;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.Statistics;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * ItemService sits between the console and the DAO.
 *
 * It owns the business rules:
 *   - which fields are required,
 *   - which dates and values are acceptable,
 *   - whether an operation makes sense (for example recovering an item
 *     that is already recovered),
 *   - whether the record exists before it is updated or deleted.
 *
 * It contains no SQL and no menu code, which keeps both of those simple.
 */
public class ItemService {

    private final ItemDAO itemDao;

    public ItemService(ItemDAO itemDao) {
        this.itemDao = itemDao;
    }

    // ------------------------------------------------------------------
    // Create
    // ------------------------------------------------------------------

    /** Saves a LOST report with the initial status ACTIVE. */
    public int reportLostItem(Item item) throws SQLException {
        return register(item, Item.TYPE_LOST);
    }

    /** Saves a FOUND report with the initial status ACTIVE. */
    public int registerFoundItem(Item item) throws SQLException {
        return register(item, Item.TYPE_FOUND);
    }

    private int register(Item item, String reportType) throws SQLException {
        item.setReportType(reportType);
        item.setStatus(Item.STATUS_ACTIVE);
        validate(item);
        return itemDao.addItem(item);
    }

    // ------------------------------------------------------------------
    // Read
    // ------------------------------------------------------------------

    public List<Item> getAllItems() throws SQLException {
        return itemDao.getAllItems();
    }

    public List<Item> getLostItems() throws SQLException {
        return itemDao.getItemsByReportType(Item.TYPE_LOST);
    }

    public List<Item> getFoundItems() throws SQLException {
        return itemDao.getItemsByReportType(Item.TYPE_FOUND);
    }

    /** Returns null when the ID does not exist. */
    public Item getItemById(int itemId) throws SQLException {
        requireValidId(itemId);
        return itemDao.getItemById(itemId);
    }

    public List<Item> searchItems(Integer itemId, String name, String category,
                                  String location, String reportType, String status) throws SQLException {
        return itemDao.searchItems(itemId, name, category, location, reportType, status);
    }

    public Statistics getStatistics() throws SQLException {
        return itemDao.getStatistics();
    }

    // ------------------------------------------------------------------
    // Update
    // ------------------------------------------------------------------

    /**
     * Saves the proposed values for an existing record.
     * Throws IllegalArgumentException when the record is missing or the
     * proposed values break a business rule.
     */
    public boolean updateItem(Item proposed) throws SQLException {
        if (proposed.getItemId() == null) {
            throw new IllegalArgumentException("Invalid item ID. Please enter a positive number.");
        }
        requireValidId(proposed.getItemId());
        requireExisting(proposed.getItemId());
        validate(proposed);
        return itemDao.updateItem(proposed);
    }

    /**
     * Marks an ACTIVE item as RECOVERED.
     *
     * The report type is never touched here, so a recovered lost item
     * stays a LOST report and a recovered found item stays a FOUND report.
     */
    public boolean markAsRecovered(int itemId) throws SQLException {
        Item existing = requireExisting(itemId);
        if (Item.STATUS_RECOVERED.equals(existing.getStatus())) {
            throw new IllegalArgumentException(
                    "Item ID " + itemId + " has already been recovered. No changes were made.");
        }
        return itemDao.markAsRecovered(itemId);
    }

    /** Removes an existing record. Throws when the ID does not exist. */
    public boolean deleteItem(int itemId) throws SQLException {
        requireExisting(itemId);
        return itemDao.deleteItem(itemId);
    }

    // ------------------------------------------------------------------
    // Business rules
    // ------------------------------------------------------------------

    /**
     * Checks one item against every business rule and normalises the
     * values before they reach the database (trimmed text, blank optional
     * fields stored as NULL).
     */
    private void validate(Item item) {
        item.setItemName(requireText(item.getItemName(), "Item name"));
        item.setCategory(requireText(item.getCategory(), "Category"));
        item.setLocation(requireText(item.getLocation(), "Location"));
        item.setDescription(blankToNull(item.getDescription()));
        item.setContactInfo(blankToNull(item.getContactInfo()));

        if (item.getReportDate() == null) {
            throw new IllegalArgumentException("Report date is required. Use the format yyyy-mm-dd.");
        }
        if (item.getReportDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Report date cannot be in the future.");
        }
        if (Item.normalizeReportType(item.getReportType()) == null) {
            throw new IllegalArgumentException("Invalid report type. Allowed values: LOST, FOUND.");
        }
        if (Item.normalizeStatus(item.getStatus()) == null) {
            throw new IllegalArgumentException("Invalid status. Allowed values: ACTIVE, RECOVERED.");
        }
    }

    private Item requireExisting(int itemId) throws SQLException {
        requireValidId(itemId);
        Item item = itemDao.getItemById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found. Please check the item ID.");
        }
        return item;
    }

    private void requireValidId(int itemId) {
        if (itemId <= 0) {
            throw new IllegalArgumentException("Invalid item ID. Please enter a positive number.");
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required and cannot be empty.");
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
