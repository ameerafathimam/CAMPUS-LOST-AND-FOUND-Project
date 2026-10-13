package com.campus.lostfound.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Item is the data model (a "blueprint") for one lost-or-found report.
 *
 * All fields are private and can only be read or changed through getters
 * and setters. This is encapsulation: the rest of the application cannot
 * put invalid data directly into an Item object.
 */
public class Item {

    // Allowed values for reportType and status. They match the CHECK
    // constraints declared in database/schema.sql.
    public static final String TYPE_LOST = "LOST";
    public static final String TYPE_FOUND = "FOUND";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_RECOVERED = "RECOVERED";

    /** Null until the record has been inserted into MySQL. */
    private Integer itemId;
    private String itemName;
    private String category;
    private String description;
    private String reportType;
    private String location;
    private LocalDate reportDate;
    private String contactInfo;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Empty constructor used while reading user input. */
    public Item() {
    }

    /** Full constructor used when creating a new report. */
    public Item(String itemName, String category, String description,
                String reportType, String location, LocalDate reportDate,
                String contactInfo, String status) {
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.reportType = reportType;
        this.location = location;
        this.reportDate = reportDate;
        this.contactInfo = contactInfo;
        this.status = status;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** True when the item is still waiting to be reunited with its owner. */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }

    /**
     * Converts user input such as "lost" or " FOUND " into the canonical
     * value "LOST" / "FOUND". Returns null when the value is not allowed.
     * Used by both the input validator and the service layer, so the rules
     * exist only once in the whole project.
     */
    public static String normalizeReportType(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim().toUpperCase();
        return (TYPE_LOST.equals(value) || TYPE_FOUND.equals(value)) ? value : null;
    }

    /** Same idea as normalizeReportType, for ACTIVE / RECOVERED. */
    public static String normalizeStatus(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim().toUpperCase();
        return (STATUS_ACTIVE.equals(value) || STATUS_RECOVERED.equals(value)) ? value : null;
    }

    /** Full record used in detailed views and confirmation screens. */
    @Override
    public String toString() {
        return "Item ID   : " + itemId + "\n"
             + "Name      : " + itemName + "\n"
             + "Category  : " + category + "\n"
             + "Type      : " + reportType + "\n"
             + "Location  : " + location + "\n"
             + "Date      : " + reportDate + "\n"
             + "Status    : " + status + "\n"
             + "Contact   : " + (isBlank(contactInfo) ? "-" : contactInfo) + "\n"
             + "Details   : " + (isBlank(description) ? "-" : description);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
