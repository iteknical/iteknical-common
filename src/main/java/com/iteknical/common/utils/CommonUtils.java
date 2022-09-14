package com.iteknical.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * @author Tony
 */
public class CommonUtils {

    /**
     * 判断外标是不是个邮箱
     *
     * @param input
     * @return
     */
    public static boolean isEmailAddress(String input) {
        return EmailValidator.getInstance().isValid(input);
    }

    /** 中国大陆手机号正则 */
    private static final String CHINA_MAINLAND_MOBILE_PHONE_REGEX = "0?(13|14|15|17|18|19)[0-9]{9}";

    /**
     * 判断是否是手机号
     * <p>
     * 目前只支持中国大陆手机号
     * </p>
     * 
     * @param input
     * @return
     */
    public static boolean isMobilePhoneNumber(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }
        return input.matches(CHINA_MAINLAND_MOBILE_PHONE_REGEX);
    }
}
