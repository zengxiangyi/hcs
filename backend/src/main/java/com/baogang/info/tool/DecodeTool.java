package com.baogang.info.tool;

import java.util.Objects;

/**
 * 模拟Oracle DECODE函数功能的工具类
 * 功能：将表达式与多个搜索值比较，返回第一个匹配的结果
 * 用法：decode(expression, search1, result1, search2, result2, ..., defaultValue)
 * 示例：decode(value, "A", "Apple", "B", "Banana", "Unknown")
 *       - 如果value等于"A"，返回"Apple"
 *       - 如果value等于"B"，返回"Banana"
 *       - 否则返回"Unknown"
 *
 * <p><b>注意</b>：与 Oracle DECODE 不同，本方法<b>不做隐式类型转换</b>（{@code Objects.equals(1, "1")} 为 false），
 * 所有 result/defaultValue 必须与调用方接收类型一致，否则在调用方赋值处抛 ClassCastException。</p>
 */
public class DecodeTool {

    private DecodeTool() {
    }

    /**
     * 模拟 Oracle DECODE：expression 依次与 args 中成对的 search 比较（Objects.equals，null 安全），
     * 返回第一个匹配的 result；无匹配返回末位 defaultValue（奇数参数时）或 null。
     *
     * @throws IllegalArgumentException 参数不足（args < 2）时抛出
     */
    public static <T> T decode(Object expression, Object... args) {
        // 与 Oracle DECODE 一致：至少需要一组 search/result 配对
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("decode 参数不足：至少需要 (expression, search, result) 三元组");
        }

        // 计算有效配对数量（成对的search和result）
        int pairCount = args.length / 2;
        boolean hasDefault = args.length % 2 != 0;
        T defaultValue = hasDefault ? cast(args[args.length - 1]) : null;

        // 遍历所有配对，寻找第一个匹配项
        for (int i = 0; i < pairCount; i++) {
            Object search = args[2 * i];      // 搜索值
            T result = cast(args[2 * i + 1]); // 匹配结果
            // 比较表达式与搜索值（处理null的情况）
            if (Objects.equals(expression, search)) {
                return result;
            }
        }
        // 无匹配项，返回默认值（包括pairCount==0的情况）
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value) {
        return (T) value;
    }

}
