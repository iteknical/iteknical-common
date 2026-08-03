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

    /**
     * 是否运行在 Termux(Android) 环境。
     * <p>
     * Termux 的 {@code os.name} 为 Linux、与普通 Linux 无从区分，故 {@link #getOS_TYPE()} 在 Termux 上返回
     * {@link OsType#LINUX}——在内核/系统调用/路径/shell 层面 Termux 就是 Linux，绝大多数分派应与 Linux 一致。
     * 它与 Linux 的差异只在用户态/libc：Bionic 跑不了 glibc 的 bundled JDK、oshi 依赖的 JNA native 也
     * dlopen(libc.so.6) 失败。这比 OsType(系统家族) 更细，故不占用枚举值，单列此布尔量，仅在真正有别的少数处
     * （JDK 来源、硬件采集）判定，免得每个 switch 都要为 TERMUX 重述一遍 LINUX 分支。
     * </p>
     * <p>
     * 判定不走 os.name（识别不出），改用 Termux 导出的环境变量：TERMUX_VERSION，或 PREFIX 含 com.termux；
     * 任一命中即是。这些变量在冷启动与 Runtime.exec 拉起的子进程中均被继承，故各执行路径判定一致。
     * 运行期不变，启动时判定一次并缓存。
     * </p>
     */
    @Getter
    private static final boolean termux  = detectTermux();

    private static boolean detectTermux() {
        if (System.getenv("TERMUX_VERSION") != null) {
            return true;
        }
        String prefix = System.getenv("PREFIX");
        return prefix != null && prefix.contains("com.termux");
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
