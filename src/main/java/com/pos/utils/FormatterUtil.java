package com.pos.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Clase de utilidades para formatear valores en la UI
 */
public class FormatterUtil {

    private static final Locale LOCALE = new Locale("es", "AR");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(LOCALE);
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(LOCALE);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Formatea un valor monetario
     */
    public static String formatCurrency(BigDecimal value) {
        if (value == null) {
            return CURRENCY_FORMAT.format(0);
        }
        return CURRENCY_FORMAT.format(value);
    }

    /**
     * Formatea un número con separador de miles
     */
    public static String formatNumber(Number value) {
        if (value == null) {
            return "0";
        }
        return NUMBER_FORMAT.format(value);
    }

    /**
     * Formatea un porcentaje (0-100)
     */
    public static String formatPercent(BigDecimal value) {
        if (value == null) {
            return "0%";
        }
        return value.setScale(2).toString() + "%";
    }

    /**
     * Formatea una fecha
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMATTER.format(date);
    }

    /**
     * Formatea una fecha y hora
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    /**
     * Trunca un texto largo y agrega puntos suspensivos
     */
    public static String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Convierte un String a BigDecimal, con manejo de errores
     */
    public static BigDecimal parseCurrency(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            // Eliminar símbolos de moneda y otros caracteres no numéricos
            String cleanValue = value.replaceAll("[^\\d.]", "");
            return new BigDecimal(cleanValue);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}