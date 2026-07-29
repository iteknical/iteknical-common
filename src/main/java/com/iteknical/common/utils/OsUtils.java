package com.iteknical.common.utils;

import lombok.Getter;
import org.apache.commons.lang3.SystemUtils;

/**
 * 操作系统判断工具
 * <p>
 * 运行期操作系统不会变化，故启动时判定一次并缓存。判定委托给commons-lang3的
 * {@link SystemUtils}（基于{@code os.name}前缀匹配，较为可靠）。
 * </p>
 *
 * @author Tony
 */
public class OsUtils {

    /**
     * 当前操作系统类型，进程内固定
     */
    @Getter
    private static final OsType OS_TYPE = detect();

    private static OsType detect() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return OsType.WINDOWS;
        }
        if (SystemUtils.IS_OS_MAC) {
            return OsType.MAC;
        }
        if (SystemUtils.IS_OS_LINUX) {
            return OsType.LINUX;
        }
        return OsType.UNKNOWN;
    }

    private OsUtils() {}

    /**
     * 操作系统类型
     */
    public enum OsType {
        /**
         * Windows
         */
        WINDOWS,
        /**
         * macOS
         */
        MAC,
        /**
         * Linux（含类Unix）
         */
        LINUX,
        /**
         * 未识别
         */
        UNKNOWN;
    }
}
