package com.campus.lostfound.model;

/**
 * Holds the numbers shown by the "View Statistics" menu option.
 * These values are always calculated from real MySQL rows - nothing here
 * is hardcoded.
 */
public class Statistics {

    private final int totalItems;
    private final int lostItems;
    private final int foundItems;
    private final int activeLostItems;
    private final int activeFoundItems;
    private final int recoveredItems;

    public Statistics(int totalItems, int lostItems, int foundItems,
                      int activeLostItems, int activeFoundItems, int recoveredItems) {
        this.totalItems = totalItems;
        this.lostItems = lostItems;
        this.foundItems = foundItems;
        this.activeLostItems = activeLostItems;
        this.activeFoundItems = activeFoundItems;
        this.recoveredItems = recoveredItems;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getLostItems() {
        return lostItems;
    }

    public int getFoundItems() {
        return foundItems;
    }

    public int getActiveLostItems() {
        return activeLostItems;
    }

    public int getActiveFoundItems() {
        return activeFoundItems;
    }

    public int getRecoveredItems() {
        return recoveredItems;
    }
}
