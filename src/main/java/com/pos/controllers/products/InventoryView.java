package com.pos.controllers.products;

import com.pos.config.ViewConfiguration;

public enum InventoryView {

    PRODUCT_TAB("productTab", ViewConfiguration.PRODUCT_LIST_VIEW),
    CATEGORY_TAB("categoryTab", ViewConfiguration.CATEGORY_VIEW);

    private final String tabId;
    private final String pathView;

    InventoryView(String tabId, String pathView) {
        this.tabId = tabId;
        this.pathView = pathView;
    }

    public String getTabId() {
        return tabId;
    }

    public String getPathView() {
        return pathView;
    }

    public static InventoryView getByTabId(String tabId) {
        for (InventoryView inventoryView : values()) {
            if (inventoryView.tabId.equals(tabId)) {
                return inventoryView;
            }
        }
        throw new IllegalArgumentException("No se encontro Inventory view con tabId: " + tabId);
    }
}
