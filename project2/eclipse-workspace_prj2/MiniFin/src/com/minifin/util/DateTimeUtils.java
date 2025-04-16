package com.minifin.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class DateTimeUtils {
    public static final DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter MONTH_YEAR_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");
    
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DEFAULT_DATE_FORMAT);
    }
    
    public static String formatMonthYear(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(MONTH_YEAR_FORMAT);
    }
    
    public static LocalDate getFirstDayOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1);
    }
    
    public static LocalDate getLastDayOfMonth(int year, int month) {
        return YearMonth.of(year, month).atEndOfMonth();
    }
    
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }
    
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }
    
    public static LocalDate getFirstDayOfYear(int year) {
        return LocalDate.of(year, 1, 1);
    }
    
    public static LocalDate getLastDayOfYear(int year) {
        return LocalDate.of(year, 12, 31);
    }
    
    public static String[] getMonthNames() {
        return new String[]{
            "January", "February", "March", "April", "May", "June", 
            "July", "August", "September", "October", "November", "December"
        };
    }
    
    public static int getMonthFromName(String monthName) {
        String[] months = getMonthNames();
        for (int i = 0; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(monthName)) {
                return i + 1;
            }
        }
        return -1;
    }
    
    public static String getPeriodLabel(LocalDate startDate, LocalDate endDate) {
        if (startDate.getYear() == endDate.getYear() && startDate.getMonthValue() == endDate.getMonthValue()) {
            // Same month and year
            return formatMonthYear(startDate);
        } else if (startDate.getYear() == endDate.getYear()) {
            // Same year, different months
            return String.format("%s - %s", 
                startDate.format(DateTimeFormatter.ofPattern("MMM")), 
                endDate.format(DateTimeFormatter.ofPattern("MMM yyyy")));
        } else {
            // Different years
            return String.format("%s - %s", 
                startDate.format(DateTimeFormatter.ofPattern("MMM yyyy")), 
                endDate.format(DateTimeFormatter.ofPattern("MMM yyyy")));
        }
    }
}