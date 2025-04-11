package com.pos.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to check if resources exist in the classpath.
 */
public class ResourcePathChecker {

    /**
     * Checks if a resource exists at the specified path.
     *
     * @param path The resource path to check
     * @return true if the resource exists, false otherwise
     */
    public static boolean resourceExists(String path) {
        return ResourcePathChecker.class.getResource(path) != null;
    }

    /**
     * Gets a list of all resources that exist from a list of paths.
     *
     * @param paths The resource paths to check
     * @return A list of paths that exist
     */
    public static List<String> getExistingResources(String... paths) {
        List<String> existingPaths = new ArrayList<>();
        for (String path : paths) {
            if (resourceExists(path)) {
                existingPaths.add(path);
            }
        }
        return existingPaths;
    }

    /**
     * Prints debug information about resources to the console.
     *
     * @param resourceName A description of the resources
     * @param paths The resource paths to check
     */
    public static void printResourceDebugInfo(String resourceName, String... paths) {
        System.out.println("Checking " + resourceName + " resources:");
        for (String path : paths) {
            boolean exists = resourceExists(path);
            System.out.println("  - " + path + ": " + (exists ? "EXISTS" : "NOT FOUND"));
        }
    }
}