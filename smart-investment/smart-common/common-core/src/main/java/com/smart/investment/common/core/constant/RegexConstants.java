package com.smart.investment.common.core.constant;

/**
 * 常用正则表达式常量
 */
public final class RegexConstants {

    private RegexConstants() {
        throw new UnsupportedOperationException("常量类不可实例化");
    }

    // ==================== 用户相关 ====================

    /** 用户名：4-20位字母数字下划线，字母开头 */
    public static final String USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{3,19}$";

    /** 密码：至少8位，包含大小写字母和数字 */
    public static final String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

    // ==================== 联系方式 ====================

    /** 手机号（中国大陆） */
    public static final String PHONE = "^1[3-9]\\d{9}$";

    /** 邮箱 */
    public static final String EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    // ==================== 证券编码 ====================

    /** 股票代码：6位数字 */
    public static final String STOCK_CODE = "^\\d{6}$";

    // ==================== 日期 ====================

    /** 日期格式：yyyy-MM-dd */
    public static final String DATE = "^\\d{4}-\\d{2}-\\d{2}$";

    /** 日期时间格式：yyyy-MM-dd HH:mm:ss */
    public static final String DATE_TIME = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";

    // ==================== 文件 ====================

    /** 文件名（不允许特殊字符，仅允许字母数字中英文下划线连字符点号） */
    public static final String FILE_NAME = "^[a-zA-Z0-9\\u4e00-\\u9fa5_.-]+$";

    // ==================== URL ====================

    /** HTTP/HTTPS URL */
    public static final String URL = "^https?://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?$";
}
