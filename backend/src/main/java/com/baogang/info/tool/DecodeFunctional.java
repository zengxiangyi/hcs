package com.baogang.info.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 用函数式接口实现类似 Oracle DECODE 的求值器（支持灵活的匹配规则）。
 *
 * <p><b>用法</b>：{@code DecodeFunctional.of(x).when(c1, r1).when(c2, r2).orElse(d).result()}</p>
 * <p><b>语义</b>：{@link #result()} 求值一次后缓存结果，可重复调用且幂等；
 * 求值后不允许再添加规则。若无匹配且未调用 {@link #orElse}，返回 {@code null}。</p>
 * <p><b>注意</b>：本类<b>非线程安全</b>，仅限单线程/局部链式调用使用。</p>
 */
public class DecodeFunctional<T, R> {
    // 存储匹配规则：Predicate（条件）-> result（结果）
    private final List<Rule<T, R>> rules = new ArrayList<>();
    private final T expression;
    private R defaultValue;
    private boolean defaultSet = false;
    private R computed;
    private boolean evaluated = false;

    private DecodeFunctional(T expression) {
        this.expression = expression;
    }

    public static <T, R> DecodeFunctional<T, R> of(T expression) {
        return new DecodeFunctional<>(expression);
    }

    /** 添加相等匹配规则（DECODE 最常用场景，Objects.equals 空安全） */
    public DecodeFunctional<T, R> whenEquals(T value, R result) {
        return when(v -> Objects.equals(v, value), result);
    }

    // 添加匹配规则：满足Predicate条件时返回result
    public DecodeFunctional<T, R> when(Predicate<T> condition, R result) {
        if (condition == null) {
            throw new IllegalArgumentException("条件不能为null");
        }
        if (evaluated) {
            throw new IllegalStateException("已经执行过匹配，不能再添加规则");
        }
        rules.add(new Rule<>(condition, result));
        return this;
    }

    /** 设置无匹配时的默认值；重复设置视为调用方误用，抛 IllegalStateException */
    public DecodeFunctional<T, R> orElse(R defaultValue) {
        if (evaluated) {
            throw new IllegalStateException("已经执行过匹配，不能再设置默认值");
        }
        if (defaultSet) {
            throw new IllegalStateException("默认值已设置，不能重复设置");
        }
        this.defaultValue = defaultValue;
        defaultSet = true;
        return this;
    }

    /**
     * 执行匹配：返回第一个满足条件的结果，无匹配返回默认值。
     * 结果求值后缓存，重复调用幂等（不重复执行 Predicate）。
     */
    public R result() {
        if (!evaluated) {
            computed = rules.stream()
                    .filter(rule -> rule.condition.test(expression))  // 测试条件是否满足
                    .map(rule -> rule.result)
                    .findFirst()
                    .orElse(defaultValue);  // 无匹配时返回默认值
            evaluated = true;
        }
        return computed;
    }

    // 内部类：封装匹配条件和结果
    private static class Rule<T, R> {
        private final Predicate<T> condition;
        private final R result;

        Rule(Predicate<T> condition, R result) {
            this.condition = condition;
            this.result = result;
        }
    }
}
