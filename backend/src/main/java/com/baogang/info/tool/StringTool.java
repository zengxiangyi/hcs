package com.baogang.info.tool;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.regex.Pattern;
/**
 * 字符串工具类
 */
public class StringTool {

    // STRICT + uuuu：拒绝 2023-02-31 等不存在的日期（SMART 会静默调整为 02-28）
    private static final String DATE_REGEX1 = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final Pattern DATE_PATTERN1 = Pattern.compile(DATE_REGEX1);
    private static final DateTimeFormatter DATE_FORMATTER1 = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final String DATE_REGEX2 = "^\\d{4}\\d{2}\\d{2}$";
    private static final Pattern DATE_PATTERN2 = Pattern.compile(DATE_REGEX2);
    private static final DateTimeFormatter DATE_FORMATTER2 = DateTimeFormatter.ofPattern("uuuuMMdd")
            .withResolverStyle(ResolverStyle.STRICT);
    // 各类型正则常量（预编译Pattern提升性能）
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern POSITIVE_INTEGER_PATTERN = Pattern.compile("^[1-9]\\d*$");
    // 参考 RFC 5322 的常用邮箱正则：支持 + 号、点等常见字符；local part 以点分段且不允许连续点/首尾点（拒绝 a..b@、.a@、a.@）
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{8}$");
    private static final Pattern CHINESE_PATTERN = Pattern.compile("^[\u4e00-\u9fa5]+$");
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    // 固定电话（可选区号+7/8位号码）及 400/800 业务号码
    private static final Pattern TEL_PATTERN = Pattern.compile("^((\\d{3,4}-?)?\\d{7,8}|[48]00-?\\d{3}-?\\d{4})$");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("^[0-9]{16,19}$");
    private StringTool() {
    }

    private static int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    public static boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isAnyBlank(CharSequence... css) {
        if (css == null || css.length == 0) {
            return false;
        } else {
            for (CharSequence cs : css) {
                if (isBlank(cs)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isAllNotBlank(CharSequence... css) {
       return !isAnyBlank(css);
    }

    /**
     * 按正则拆分字符串，并过滤掉空串。
     * <p>注意：与 {@link String#split(String)} 不同，本方法会移除结果中的空字符串
     * （包括尾部空串），且对空白分隔符同样过滤。</p>
     *
     * @param str   待拆分字符串
     * @param regex 分隔正则；为空时按单个空格拆分
     * @return 过滤空串后的字符串数组
     */
    public static String[] split(String str, String regex) {
        if (isBlank(str)) {
            return new String[0];
        }
        if (isBlank(regex)) {
            regex = " ";
        }
        String[] data = str.split(regex);
        // 删除空白字符串并去除前后空格（"a , b" → ["a","b"]）
        data = Arrays.stream(data).map(String::trim).filter(s -> !isBlank(s)).toArray(String[]::new);
        return data;
    }

    public static boolean isDate(String dateStr) {
        if (isBlank(dateStr)) {
            return false;
        }
        return isValidDate(dateStr, DATE_PATTERN1, DATE_FORMATTER1) 
            || isValidDate(dateStr, DATE_PATTERN2, DATE_FORMATTER2);
    }

    private static boolean isValidDate(String dateStr, Pattern pattern, DateTimeFormatter formatter) {
        if (pattern.matcher(dateStr).matches()) {
            try {
                // LocalDate.parse 会做跨字段有效性校验（如 2 月 30 日被拒绝）
                LocalDate.parse(dateStr, formatter);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        }
        return false;
    }

    // 正则表达式判断字符串是数字
    public static boolean isNumeric(String str) {
        if (isBlank(str)) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(str).matches();
    }

    public static boolean isPositiveInteger(String str) {
        if (isBlank(str)) {
            return false;
        }
        return POSITIVE_INTEGER_PATTERN.matcher(str).matches();
    }

    public static boolean isEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isPhone(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isUrl(String url) {
        if (isBlank(url)) {
            return false;
        }
        try {
            java.net.URI uri = new java.net.URI(url.trim());
            String scheme = uri.getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http")
                    || scheme.equalsIgnoreCase("https")
                    || scheme.equalsIgnoreCase("ftp")
                    || scheme.equalsIgnoreCase("file"));
        } catch (java.net.URISyntaxException e) {
            return false;
        }
    }

    public static boolean isChinese(String chinese) {
        if (isBlank(chinese)) {
            return false;
        }
        return CHINESE_PATTERN.matcher(chinese).matches();
    }

    public static boolean isEnglish(String english) {
        if (isBlank(english)) {
            return false;
        }
        return ENGLISH_PATTERN.matcher(english).matches();
    }

    public static boolean isTel(String tel) {
        if (isBlank(tel)) {
            return false;
        }
        return TEL_PATTERN.matcher(tel).matches();
    }

    /**
     * 验证 18 位身份证号：先做格式校验，再按 GB 11643 校验位算法验证第 18 位。
     */
    public static boolean isIdCard(String idCard) {
        if (isBlank(idCard) || !ID_CARD_PATTERN.matcher(idCard).matches()) {
            return false;
        }
        return isValidIdCardCheckDigit(idCard);
    }

    // GB 11643 身份证校验位权重
    private static final int[] ID_CARD_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    // 余数 0-10 对应的校验码
    private static final char[] ID_CARD_CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private static boolean isValidIdCardCheckDigit(String idCard) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard.charAt(i) - '0') * ID_CARD_WEIGHTS[i];
        }
        char expected = ID_CARD_CHECK_CODES[sum % 11];
        return Character.toUpperCase(idCard.charAt(17)) == expected;
    }

    public static boolean isBankCard(String bankCard) {
        if (isBlank(bankCard)) {
            return false;
        }
        return BANK_CARD_PATTERN.matcher(bankCard).matches();
    }

}
