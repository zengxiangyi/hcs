package com.baogang.info.tool;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 时间窗口工具类，用于获取调度周期的时间区间。
 *
 * <p><b>区间语义：统一为半开区间 [start, end)</b> —— 上界为下一整点 / 次日 00:00:00，
 * SQL 查询请使用 {@code >= start AND < end}，避免"末刻减1"在亚秒精度下丢数据。</p>
 *
 * <p><b>时区：依赖系统默认时区（Asia/Shanghai，无 DST）。</b>所有窗口取自同一次 {@code now()}，
 * 不存在跨整点取值错位问题。非线程共享问题不存在（无状态静态方法，线程安全）。</p>
 */
public class TimeWindow {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimeWindow() {
    }

    /**
     * 获取前2小时窗口 [两小时前整点, 一小时前整点) 的开始时间字符串。
     * <p>例如：当前时间为 15:30:45，则返回 "13:00:00"。</p>
     */
    public static String getTowHourBeforeStart() {
        return truncateToHour(LocalDateTime.now().minusHours(2)).format(FORMATTER);
    }

    /**
     * 获取前2小时窗口 [两小时前整点, 一小时前整点) 的结束上界（半开区间，不含该值）。
     * <p>例如：当前时间为 15:30:45，则返回 "14:00:00"。SQL 上界用 {@code <} 而非 {@code <=}。</p>
     */
    public static String getTwoHourBeforeEnd() {
        return truncateToHour(LocalDateTime.now().minusHours(1)).format(FORMATTER);
    }

    /**
     * 获取前一小时窗口 [前一小时整点, 当前整点) 的开始时间戳（毫秒）。
     * <p>例如：当前时间为 15:30:45，则返回 14:00:00 对应的毫秒时间戳。</p>
     */
    public static long getStartTimestamp() {
        return truncateToHour(LocalDateTime.now().minusHours(1))
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取前一小时窗口 [前一小时整点, 当前整点) 的结束上界时间戳（毫秒，半开区间，不含该值）。
     * <p>例如：当前时间为 15:30:45，则返回 15:00:00 对应的毫秒时间戳。</p>
     */
    public static long getEndTimestamp() {
        return truncateToHour(LocalDateTime.now())
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /** 指定日期的开始（当天 00:00:00） */
    public static String getDateBegin(LocalDate localDate) {
        if(localDate!=null) {
            return localDate.atStartOfDay().format(FORMATTER);
        }
        return null;
    }

    /**
     * 指定日期的结束上界（次日 00:00:00，半开区间，不含该值）。
     * <p>SQL 上界用 {@code <} 而非 {@code <=}，避免丢亚秒精度数据。</p>
     */
    public static String getDateEnd(LocalDate localDate) {
        if(localDate!=null) {
            return localDate.plusDays(1).atStartOfDay().format(FORMATTER);
        }
        return null;
    }

    public static String getTodayBegin() {
        return getDateBegin(LocalDate.now());
    }

    public static String getTodayEnd() {
        return getDateEnd(LocalDate.now());
    }

    /** 截断到整点（时、分、秒、纳秒清零） */
    private static LocalDateTime truncateToHour(LocalDateTime time) {
        return time.truncatedTo(ChronoUnit.HOURS);
    }

}
