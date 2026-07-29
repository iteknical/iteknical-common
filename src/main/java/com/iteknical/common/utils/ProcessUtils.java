package com.iteknical.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 外部进程执行工具
 * <p>
 * 跨平台地运行一个程序并等待其结束，把标准输出与标准错误合并写入指定文件。等价于Windows下
 * {@code CMD /C START /B /WAIT <program> ... >outputFile 2>&1} 的效果，但改用{@link ProcessBuilder}
 * 自带的重定向实现，因此在Windows/macOS/Linux上行为一致，无需{@code CMD}包裹或shell重定向记号。
 * </p>
 *
 * @author Tony
 */
public class ProcessUtils {

    /**
     * 运行程序并等待结束，stdout与stderr合并写入outputFile（覆盖），返回退出码
     *
     * @param command          程序及其参数，不含CMD包裹、不含"&gt;out 2&gt;&amp;1"记号。
     *                         注意：程序名的查找基于当前进程工作目录+PATH，{@code workingDirectory}对其无效；
     *                         若二进制位于某目录内（Win/Linux自带包模式），须传其绝对路径或相对当前进程工作目录的路径，
     *                         而非裸文件名；仅当二进制在PATH中（如macOS的brew）时才可用裸名"hashcat"
     * @param workingDirectory 工作目录（进程运行时的cwd，如hashcat读写temp/potfile的位置），为null时继承当前进程工作目录
     * @param outputFile       stdout+stderr的输出文件，为null时继承当前进程的输出
     * @return 进程退出码
     */
    public static int run(List<String> command, File workingDirectory, File outputFile)
        throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        if (workingDirectory != null) {
            processBuilder.directory(workingDirectory);
        }

        // 合并stderr到stdout，等价于 2>&1
        processBuilder.redirectErrorStream(true);

        if (outputFile != null) {
            // 覆盖写入，等价于 >outputFile
            processBuilder.redirectOutput(ProcessBuilder.Redirect.to(outputFile));
        }

        Process process = processBuilder.start();
        // 输出直连文件（非管道），无需读流即可安全等待，不会死锁
        return process.waitFor();
    }

    private ProcessUtils() {}
}
