package com.smart.investment.common.core.utils;

/**
 * 字符串处理工具类
 */
public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    // ==================== 判空 ====================

    /**
     * 字符串是否为空（null 或空串）
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.isEmpty();
    }

    /**
     * 字符串是否非空
     */
    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * 字符串是否为空白（null、空串或纯空白字符）
     */
    public static boolean isBlank(CharSequence cs) {
        if (cs == null || cs.isEmpty()) {
            return true;
        }
        for (int i = 0; i < cs.length(); i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 字符串是否非空白
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 安全 toString（null 返回空串）
     */
    public static String nullToEmpty(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    // ==================== 截取与脱敏 ====================

    /**
     * 截断到指定长度（超出部分用 "..." 替代）
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * 手机号脱敏（保留前3位和后4位）
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏（保留首字符和@后内容）
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "*" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    // ==================== 大小写与命名转换 ====================

    /**
     * 驼峰 → 下划线（如 userName → user_name）
     */
    public static String camelToUnderline(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 下划线 → 驼峰（如 user_name → userName）
     */
    public static String underlineToCamel(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                sb.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        return sb.toString();
    }

    // ==================== 校验 ====================

    /**
     * 是否匹配指定正则
     */
    public static boolean matches(String str, String regex) {
        return str != null && str.matches(regex);
    }
}
