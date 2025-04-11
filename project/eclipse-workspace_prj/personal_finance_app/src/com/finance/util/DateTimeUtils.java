
/**********************************************
Project
Course: APD545
Last Name: Chu
First Name: Sin Kau
ID: 155131220
Section: NDD
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature Sin Kau Chu
Date: 11-Apr-2025
**********************************************/

package com.finance.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations
 */
public class DateTimeUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    
    /**
     * Format a LocalDate as MM/dd/yyyy
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * Format a YearMonth as "Month Year" (e.g., "January 2023")
     */
    public static String formatYearMonth(YearMonth yearMonth) {
        if (yearMonth == null) {
            return "";
        }
        return yearMonth.format(MONTH_FORMATTER);
    }
    
    /**
     * Get the start date of a month
     */
    public static LocalDate getMonthStartDate(YearMonth yearMonth) {
        return yearMonth.atDay(1);
    }
    
    /**
     * Get the end date of a month
     */
    public static LocalDate getMonthEndDate(YearMonth yearMonth) {
        return yearMonth.atEndOfMonth();
    }
    
    /**
     * Get the current month as YearMonth
     */
    public static YearMonth getCurrentMonth() {
        return YearMonth.now();
    }
    
    /**
     * Get a sequence of months from startMonth (inclusive) for the specified number of months
     */
    public static YearMonth[] getMonthSequence(YearMonth startMonth, int numMonths) {
        YearMonth[] months = new YearMonth[numMonths];
        for (int i = 0; i < numMonths; i++) {
            months[i] = startMonth.plusMonths(i);
        }
        return months;
    }
}