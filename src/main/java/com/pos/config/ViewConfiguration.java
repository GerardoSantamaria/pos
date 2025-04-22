package com.pos.config;

/**
 * Configuration class containing constants for view paths.
 */
public class ViewConfiguration {

    /**
     * Path to the login view FXML.
     */
    public static final String LOGIN_VIEW = "/templates/login.fxml";

    /**
     * Path to the main dashboard view FXML.
     */
    public static final String DASHBOARD_VIEW = "/templates/dashboard.fxml";

    /**
     * Path to the product list view FXML.
     */
    public static final String PRODUCT_LIST_VIEW = "/templates/product/list.fxml";

    /**
     * Path to the product form view FXML.
     */
    public static final String PRODUCT_FORM_VIEW = "/templates/product/form.fxml";

    /**
     * Path to the dashboard invetory form view FXML.
     */
    public static final String INVENTORY_DASHBOARD_VIEW = "/templates/product/inventory-dashboard.fxml";

    public static final String CATEGORY_VIEW = "/templates/product/category.fxml";
    public static final String CATEGORY_FORM_VIEW = "/templates/product/category-form.fxml";
    public static final String INVENTORY_DASHBOARD = "/templates/product/inventory-dashboard.fxml";

    /**
     * Path to the user management view FXML.
     */
    public static final String USER_MANAGEMENT_VIEW = "/templates/user/management.fxml";

    /**
     * Path to the user form view FXML.
     */
    public static final String USER_FORM_VIEW = "/templates/user/form.fxml";

    /**
     * Private constructor to prevent instantiation.
     */
    private ViewConfiguration() {
        // Empty constructor to prevent instantiation
    }
}