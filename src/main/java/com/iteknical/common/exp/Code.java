package com.iteknical.common.exp;

/**
 * 异常码
 */
public class Code {
    /*
     * 异常码一共为5位，规范如下：
     * 0为成功，无异常
     * 第一位为1代表通用业务异常，各业务不做自定义；
     * 第一位为1代表各业务自己的业务业务异常，各业务系统自己定义和细分
     * 第一位为9代表通用的未预期异常，统一定义，各业务不做自定义
     */

    /** 成功 */
    int SUCCESS           = 0;

    /** 入参无效 */
    int PARAMETER_INVALID = 10101;
    /** 上锁错误 */
    int LOCK_FAILED       = 10201;

    /** 未预期错误 */
    int UNEXPECTED_ERROR  = 90000;
}