package com.baogang.info.tool;

import com.baogang.info.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

/**
 * 日期和时间工具类
 */
public class DateTimeTool {
    // 常用日期格式（STRICT + uuuu：拒绝 2 月 30 日等不存在日期，SMART 会静默调整）
    private static final String YYYY_MM_DD = "uuuu-MM-dd";
    private static final String YYYY_MM_DD_HH_MM_SS = "uuuu-MM-dd HH:mm:ss";
    private static final String YYYYMMDD = "uuuuMMdd";
    private static final String YYYYMMDDHHMMSS = "uuuuMMddHHmmss";
    private static final String YYYYMMDDHHMMSS_SSS = "uuuuMMddHHmmss.SSS";
    // 静态常量复用一次创建
    private static final DateTimeFormatter DATE_FORMATTER = strict(YYYY_MM_DD);
    private static final DateTimeFormatter DATETIME_FORMATTER = strict(YYYY_MM_DD_HH_MM_SS);
    private static final DateTimeFormatter NUM_DATE_FORMATTER = strict(YYYYMMDD);
    private static final DateTimeFormatter NUM_DATETIME_FORMATTER = strict(YYYYMMDDHHMMSS);
    private static final DateTimeFormatter NUM_DATETIME_FORMATTER_SSS = strict(YYYYMMDDHHMMSS_SSS);

    /** STRICT 解析模式格式器：pattern 中 yyyy 由调用方负责（本类常量均用 uuuu） */
    private static DateTimeFormatter strict(String pattern) {
        return DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    }

    private static final Logger log = LoggerFactory.getLogger(DateTimeTool.class);

    /** 统一时区：不随 JVM/容器 systemDefault 漂移（跨环境部署 UTC 容器时避免 8 小时偏移） */
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private DateTimeTool() {
    }

    /**
     * 验证字符串是否为指定格式的日期
     */
    public static boolean isValidDate(String dateStr, String pattern) {
        if (StringTool.isBlank(dateStr) || StringTool.isBlank(pattern)) {
            return false;
        }
        try {
            DateTimeFormatter formatter = getFormatter(pattern);
            String strictPattern = toStrictPattern(pattern);
            // 日期验证（yyyy-MM-dd / yyyyMMdd）
            if (strictPattern.equals(YYYY_MM_DD) || strictPattern.equals(YYYYMMDD)) {
                LocalDate.parse(dateStr, formatter);
            }
            // 日期时间验证（yyyy-MM-dd HH:mm:ss / yyyyMMddHHmmss / yyyyMMddHHmmss.SSS）
            else if (strictPattern.equals(YYYY_MM_DD_HH_MM_SS) || strictPattern.equals(YYYYMMDDHHMMSS) || strictPattern.equals(YYYYMMDDHHMMSS_SSS)) {
                LocalDateTime.parse(dateStr, formatter);
            }
            // 其他格式通用解析 - 根据pattern是否包含时间部分选择解析器
            else if (strictPattern.contains("HH") || strictPattern.contains("hh")) {
                LocalDateTime.parse(dateStr, formatter);
            } else {
                LocalDate.parse(dateStr, formatter);
            }
            return true;
        } catch (DateTimeParseException | IllegalArgumentException e) {
            // IllegalArgumentException：非法 pattern 也按"校验不通过"返回，不破坏 boolean 契约
            return false;
        }
    }

    /** STRICT 模式要求 year-of-era 用 uuuu 表示，统一替换 */
    private static String toStrictPattern(String pattern) {
        return pattern.replace('y', 'u');
    }

    private static DateTimeFormatter getFormatter(String pattern) {
        String strictPattern = toStrictPattern(pattern);
        if (YYYY_MM_DD.equals(strictPattern)) {
            return DATE_FORMATTER;
        }
        if (YYYY_MM_DD_HH_MM_SS.equals(strictPattern)) {
            return DATETIME_FORMATTER;
        }
        if (YYYYMMDD.equals(strictPattern)) {
            return NUM_DATE_FORMATTER;
        }
        if (YYYYMMDDHHMMSS.equals(strictPattern)) {
            return NUM_DATETIME_FORMATTER;
        }
        if (YYYYMMDDHHMMSS_SSS.equals(strictPattern)) {
            return NUM_DATETIME_FORMATTER_SSS;
        }
        // 自定义 pattern 每次新建（ofPattern 开销可接受，避免缓存满后永久失效的行为突变）
        return strict(strictPattern);
    }

    // 快速验证：yyyy-MM-dd
    public static boolean isDate(String dateStr) {
        return isValidDate(dateStr, YYYY_MM_DD);
    }

    // 快速验证：yyyyMMdd
    public static boolean isNumDate(String dateStr) {
        return isValidDate(dateStr, YYYYMMDD);
    }

    // 快速验证：yyyy-MM-dd HH:mm:ss
    public static boolean isDateTime(String dateStr) {
        return isValidDate(dateStr, YYYY_MM_DD_HH_MM_SS);
    }

    public static String currentTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public static String currentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String currentNumTime() {
        return LocalDateTime.now().format(NUM_DATETIME_FORMATTER);
    }

    public static String yesterday() {
        return LocalDate.now().minusDays(1L).format(DATE_FORMATTER);
    }

    public static String tomorrow() {
        return LocalDate.now().plusDays(1L).format(DATE_FORMATTER);
    }

    public static String tomorrowNum() {
        return LocalDate.now().plusDays(1L).format(NUM_DATE_FORMATTER);
    }

    public static String todayNum() {
        return LocalDate.now().format(NUM_DATE_FORMATTER);
    }

    public static String simpleDateString(LocalDate date) {
        if (date == null) {
            throw new BusinessException(3003, "simpleDateString 参数不能为 null");
        }
        return date.format(DATE_FORMATTER);
    }

    public static String simpleDateNum(LocalDate date) {
        if (date == null) {
            throw new BusinessException(3003, "simpleDateNum 参数不能为 null");
        }
        return date.format(NUM_DATE_FORMATTER);
    }

    public static Date changeToDate(LocalDateTime time) {
        if (time == null) {
            throw new BusinessException(3003, "changeToDate 参数不能为 null");
        }
        return Date.from(time.atZone(ZONE).toInstant());
    }

    public static String dateToString(Date time) {
        if (time == null) {
            throw new BusinessException(3003, "dateToString 参数不能为 null");
        }
        return time.toInstant().atZone(ZONE).toLocalDateTime().format(NUM_DATETIME_FORMATTER_SSS);
    }

    public static Date stringToDate(String time) {
        if (StringTool.isBlank(time)) {
            throw new BusinessException(3003, "stringToDate 参数不能为空");
        }
        try {
            return Date.from(LocalDateTime.parse(time, DATETIME_FORMATTER).atZone(ZONE).toInstant());
        } catch (DateTimeParseException e) {
            throw new BusinessException(3003, e.getMessage());
        }
    }

    public static LocalDate stringToLocalDate(String time) {
        if (StringTool.isNotBlank(time)) {
            try {
                return parseLenToLocalDate(time.trim());
            } catch (DateTimeParseException e) {
                throw new BusinessException(3003, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 按字符串长度统一解析为 LocalDateTime（纯日期取当天零点）。
     * 长度无法识别时抛 DateTimeParseException，由上层统一处理异常来源。
     */
    private static LocalDateTime parseLenToDateTime(String time) {
        switch (time.length()) {
            case 8:
                return LocalDate.parse(time, NUM_DATE_FORMATTER).atStartOfDay();
            case 10:
                return LocalDate.parse(time, DATE_FORMATTER).atStartOfDay();
            case 14:
                return LocalDateTime.parse(time, NUM_DATETIME_FORMATTER);
            case 18:
                return LocalDateTime.parse(time, NUM_DATETIME_FORMATTER_SSS);
            case 19:
                return LocalDateTime.parse(time, DATETIME_FORMATTER);
            default:
                throw new DateTimeParseException("无法识别的日期格式，长度=" + time.length() + ", 值=" + time, time, 0);
        }
    }

    /**
     * 按字符串长度匹配格式并解析为 LocalDate
     */
    private static LocalDate parseLenToLocalDate(String time) {
        return parseLenToDateTime(time).toLocalDate();
    }

    /**
     * 获取 LocalDateTime 对应的微秒时间戳（自 Epoch 起，真实微秒精度）。
     */
    public static long getTimeMicroVal(LocalDateTime time) {
        if (time == null) {
            throw new BusinessException(3003, "getTimeMicroVal 参数不能为 null");
        }
        Instant instant = time.atZone(ZONE).toInstant();
        return ChronoUnit.MICROS.between(Instant.EPOCH, instant);
    }

    public static LocalDateTime parseDateTime(String datetime) {
        if (StringTool.isNotBlank(datetime)) {
            try {
                return LocalDateTime.parse(datetime, DATETIME_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new BusinessException(3003, e.getMessage());
            }
        }
        return null;
    }

    public static String localDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            throw new BusinessException(3003, "localDateTimeToString 参数不能为 null");
        }
        return localDateTime.format(DATETIME_FORMATTER);
    }

    public static String localDateToString(LocalDate localDate) {
        if (localDate == null) {
            throw new BusinessException(3003, "localDateToString 参数不能为 null");
        }
        return localDate.format(DATE_FORMATTER);
    }

    public static String localDateToNum(LocalDate localDate) {
        if (localDate == null) {
            throw new BusinessException(3003, "localDateToNum 参数不能为 null");
        }
        return localDate.format(NUM_DATE_FORMATTER);
    }

    public static String getTimeNum() {
        return LocalDateTime.now().format(NUM_DATETIME_FORMATTER);
    }

    public static String dealTimeString(String dateTime) {
        if (StringTool.isNotBlank(dateTime)) {
            try {
                return convertLenToFormatted(dateTime);
            } catch (DateTimeParseException e) {
                throw new BusinessException(3003, e.getMessage());
            }
        }
        return "";
    }

    /**
     * 按字符串长度匹配格式，解析并统一输出 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss
     */
    private static String convertLenToFormatted(String dateTime) {
        LocalDateTime dt = parseLenToDateTime(dateTime);
        // 长度 >= 14 视为含时间部分，输出带时间格式；否则仅输出日期
        return dateTime.length() >= 14 ? dt.format(DATETIME_FORMATTER) : dt.format(DATE_FORMATTER);
    }

    public static String formatDateStart(String startTime) {
        return formatDateWindow(startTime, "formatDateStart", TimeWindow::getDateBegin);
    }

    public static String formatDateEnd(String endTime) {
        return formatDateWindow(endTime, "formatDateEnd", TimeWindow::getDateEnd);
    }

    private static String formatDateWindow(String time, String methodName, Function<LocalDate, String> windowFn) {
        try {
            LocalDate localDate = stringToLocalDate(time);
            if (localDate == null) {
                log.warn("{} 无法解析日期，降级返回原值: {}", methodName, time);
                return time;
            }
            return windowFn.apply(localDate);
        } catch (BusinessException e) {
            log.warn("{} 无法解析日期，降级返回原值: {}", methodName, time);
            return time;
        }
    }

    public static String dateNumToString(String dateNum) {
        if (StringTool.isNotBlank(dateNum) && dateNum.matches("^\\d{8}$")) {
            try {
                return LocalDate.parse(dateNum, NUM_DATE_FORMATTER).format(DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new BusinessException(3003, "无效的数字日期: " + dateNum);
            }
        }
        log.warn("dateNumToString 无效的8位数字日期，降级返回空字符串: {}", dateNum);
        return "";
    }

}