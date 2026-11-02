package com.campus.recycle.util;

import com.campus.recycle.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * 输入合法性校验工具（防御性编程）。
 *
 * <p>集中定义各业务字段的合法性规则，所有服务层在写入前调用，杜绝非法输入
 * 进入持久层。</p>
 */
public final class Validator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final int PASSWORD_MIN = 6;
    private static final int PASSWORD_MAX = 32;

    private Validator() {
        // 工具类，禁止实例化
    }

    /** 校验用户名：3-20 位字母、数字或下划线。 */
    public static void requireUsername(String username) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException("用户名须为 3-20 位字母、数字或下划线");
        }
    }

    /** 校验密码：6-32 位，且同时包含字母与数字。 */
    public static void requirePassword(String password) {
        if (password == null || password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {
            throw new ValidationException("密码长度须为 " + PASSWORD_MIN + "-" + PASSWORD_MAX + " 位");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasLetter || !hasDigit) {
            throw new ValidationException("密码须同时包含字母和数字");
        }
    }

    /** 校验价格：非 NaN 且非负。 */
    public static void requirePrice(double price) {
        if (Double.isNaN(price) || price < 0) {
            throw new ValidationException("价格必须为非负数值");
        }
    }

    /** 校验评分：1-5 的整数。 */
    public static void requireRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("评分须为 1-5 之间的整数");
        }
    }

    /** 校验非空文本并限制最大长度。 */
    public static void requireText(String value, String fieldName, int maxLength) {
        requireNotBlank(value, fieldName);
        if (value.length() > maxLength) {
            throw new ValidationException(fieldName + "长度不能超过 " + maxLength + " 个字符");
        }
    }

    /** 校验非空。 */
    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + "不能为空");
        }
    }
}
