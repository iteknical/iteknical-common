package com.iteknical.common.exp;

import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * 异常码
 */
public class Code {
    /*
     * 异常码一共为5位，规范如下：
     * 0为成功，无异常
     * 第一位为1代表通用业务异常，各业务不做自定义；
     * 第一位为2开始代表各业务自己的业务业务异常，各业务系统自己定义和细分
     * 第一位为9代表通用的未预期异常，统一定义，各业务不做自定义
     */

    /** 成功 */
    public static final int                               SUCCESS           = 0;

    /** 入参无效 */
    public static final int                               PARAMETER_INVALID = 10101;

    /** 没有授权 */
    public static final int                               NO_AUTHORIZATION  = 10301;

    /** 未预期错误 */
    public static final int                               UNEXPECTED_ERROR  = 90000;

    public static final Map<Locale, Map<Integer, String>> CODE_TRANSLATION  =
        ImmutableMap.<Locale, Map<Integer, String>>builder()
            .put(Locale.SIMPLIFIED_CHINESE, ImmutableMap.<Integer, String>builder()
                .put(PARAMETER_INVALID, "参数非法")
                .put(NO_AUTHORIZATION, "没有授权")
                .put(UNEXPECTED_ERROR, "系统异常")
                .build())
            .build();
}
