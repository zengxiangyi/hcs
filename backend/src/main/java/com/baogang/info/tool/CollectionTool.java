package com.baogang.info.tool;

import java.util.Collection;
import java.util.List;

/**
 * 集合处理工具类
 */
public class CollectionTool {

    private CollectionTool() {
    }

    /**
     * 列表数据非空且第一个元素非空。
     * <p>注意：列表为空或首元素为 null 均返回 false。</p>
     */
    public static boolean firstItemIsNotNull(List<?> data) {
        return isNotEmptyList(data) && data.get(0) != null;
    }

    /**
     * 列表数据非空
     */
    public static boolean isNotEmptyList(List<?> data) {
        return data != null && !data.isEmpty();
    }

    /**
     * 列表数据为空（null 或空列表）
     */
    public static boolean isEmptyList(List<?> data) {
        return data == null || data.isEmpty();
    }

    /**
     * 通用集合数据非空（兼容 List/Set/Collection 实现）
     */
    public static boolean isNotEmpty(Collection<?> data) {
        return data != null && !data.isEmpty();
    }

    /**
     * 通用集合数据为空
     */
    public static boolean isEmpty(Collection<?> data) {
        return data == null || data.isEmpty();
    }

}
