package com.smart.investment.common.core.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期时间工具类
 */
public final class DateTimeUtils {

    /** 标准日期格式 */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    /** 标准日期时间格式 */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /** 标准时间格式 */
    public static final String TIME_PATTERN = "HH:mm:ss";
    /** 紧凑日期时间格式（无分隔符） */
    public static final String COMPACT_DATE_TIME_PATTERN = "yyyyMMddHHmmss";

    /** 默认时区（系统时区） */
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private DateTimeUtils() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    // ==================== 格式化 ====================

    /**
     * LocalDateTime → 字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    }

    /**
     * LocalDateTime → 字符串（指定格式）
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * LocalDate → 字符串（yyyy-MM-dd）
     */
    public static String format(LocalDate date) {
        return date == null ? null : date.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

    /**
     * LocalDate → 字符串（指定格式）
     */
    public static String format(LocalDate date, String pattern) {
        return date == null ? null : date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * LocalTime → 字符串（HH:mm:ss）
     */
    public static String format(LocalTime time) {
        return time == null ? null : time.format(DateTimeFormatter.ofPattern(TIME_PATTERN));
    }

    /**
     * 时间戳（毫秒） → 字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static String format(long timestamp) {
        return format(ofTimestamp(timestamp));
    }

    // ==================== 解析 ====================

    /**
     * 字符串 → LocalDateTime（yyyy-MM-dd HH:mm:ss）
     */
    public static LocalDateTime parseDateTime(String str) {
        return str == null ? null : LocalDateTime.parse(str, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    }

    /**
     * 字符串 → LocalDateTime（指定格式）
     */
    public static LocalDateTime parseDateTime(String str, String pattern) {
        return str == null ? null : LocalDateTime.parse(str, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 字符串 → LocalDate（yyyy-MM-dd）
     */
    public static LocalDate parseDate(String str) {
        return str == null ? null : LocalDate.parse(str, DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

    /**
     * 字符串 → LocalTime（HH:mm:ss）
     */
    public static LocalTime parseTime(String str) {
        return str == null ? null : LocalTime.parse(str, DateTimeFormatter.ofPattern(TIME_PATTERN));
    }

    // ==================== 时间戳转换 ====================

    /**
     * 时间戳（毫秒） → LocalDateTime
     */
    public static LocalDateTime ofTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), DEFAULT_ZONE);
    }

    /**
     * LocalDateTime → 时间戳（毫秒）
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        return dateTime == null ? 0L : dateTime.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    // ==================== 日期计算 ====================

    /**
     * 获取当前日期时间字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static String now() {
        return format(LocalDateTime.now());
    }

    /**
     * 获取当前日期字符串（yyyy-MM-dd）
     */
    public static String today() {
        return format(LocalDate.now());
    }

    /**
     * 获取 n 天前的日期
     */
    public static LocalDate daysAgo(int days) {
        return LocalDate.now().minusDays(days);
    }

    /**
     * 计算两个 LocalDateTime 之间的天数差
     */
    public static long daysBetween(LocalDateTime from, LocalDateTime to) {
        return ChronoUnit.DAYS.between(from, to);
    }

    /**
     * 计算两个 LocalDateTime 之间的小时差
     */
    public static long hoursBetween(LocalDateTime from, LocalDateTime to) {
        return ChronoUnit.HOURS.between(from, to);
    }

    /**
     * 判断日期是否在指定区间内
     */
    public static boolean isBetween(LocalDateTime target, LocalDateTime start, LocalDateTime end) {
        return !target.isBefore(start) && !target.isAfter(end);
    }
}
