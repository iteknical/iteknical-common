package com.iteknical.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * 跨平台进程工具
 * <p>
 * 覆盖三类操作：{@link #run 运行外部程序并等待}、{@link #list 枚举进程}、{@link #killTree 杀进程树}。
 * Windows走PowerShell/taskkill，Linux走/proc，macOS走ps。
 * </p>
 *
 * @author Tony
 */
public class ProcessUtils {

    /**
     * 运行程序并等待结束，stdout与stderr合并写入outputFile（覆盖），返回退出码
     * <p>
     * 改用{@link ProcessBuilder}自带的重定向，等价于Windows下
     * {@code CMD /C START /B /WAIT <program> ... >outputFile 2>&1} 的效果，但在Windows/macOS/Linux行为一致，
     * 无需{@code CMD}包裹或shell重定向记号。
     * </p>
     *
     * @param command          程序及其参数，不含CMD包裹、不含"&gt;out 2&gt;&amp;1"记号。
     *                         注意：程序名的查找基于当前进程工作目录+PATH，{@code workingDirectory}对其无效；
     *                         若二进制位于某目录内（Win/Linux自带包模式），须传其绝对路径或相对当前进程工作目录的路径，
     *                         而非裸文件名；仅当二进制在PATH中（如macOS的brew）时才可用裸名"hashcat"
     * @param workingDirectory 工作目录（进程运行时的cwd，如hashcat读写temp/potfile的位置），为null时继承当前进程工作目录
     * @param outputFile       stdout+stderr的输出文件，为null时继承当前进程的输出（不产生未读管道）
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
        } else {
            // 无输出文件时继承父进程输出，避免未读管道被写满导致死锁
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }

        Process process = processBuilder.start();
        // 输出直连文件/父进程（非管道），无需读流即可安全等待，不会死锁
        return process.waitFor();
    }

    /**
     * 枚举当前所有进程，返回统一的{@link ProcessDTO}（pid/ppid/path/commandLine）
     */
    public static List<ProcessDTO> list() throws IOException, InterruptedException {
        switch (OsUtils.getOS_TYPE()) {
            case WINDOWS:
                return listWindows();
            case LINUX:
                return listLinux();
            case MAC:
                return listMac();
            default:
                throw new UnsupportedOperationException("unsupported os for process listing");
        }
    }

    /**
     * 杀掉指定进程及其全部后代进程（杀树）
     * <p>
     * 出于安全，绝不杀掉当前进程自身。
     * </p>
     *
     * @param pid 目标进程id
     */
    public static void killTree(int pid) throws IOException, InterruptedException {
        if (OsUtils.getOS_TYPE() == OsUtils.OsType.WINDOWS) {
            // taskkill的/T即为杀树
            run(Arrays.asList("taskkill", "/PID", String.valueOf(pid), "/T", "/F"), null, null);
            return;
        }

        // POSIX：由进程快照自行计算后代，一次性kill -9
        List<ProcessDTO> all = list();
        Map<Integer, List<Integer>> childrenByPpid = new LinkedHashMap<>();
        for (ProcessDTO processDTO : all) {
            if (processDTO.getPpid() == null || processDTO.getPid() == null) {
                continue;
            }
            childrenByPpid.computeIfAbsent(processDTO.getPpid(), k -> new ArrayList<>()).add(processDTO.getPid());
        }

        Set<Integer> toKill = new LinkedHashSet<>();
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(pid);
        while (!queue.isEmpty()) {
            Integer cur = queue.poll();
            if (!toKill.add(cur)) {
                continue;
            }
            List<Integer> children = childrenByPpid.get(cur);
            if (children != null) {
                queue.addAll(children);
            }
        }

        // 绝不杀自己
        toKill.remove(currentPid());
        if (toKill.isEmpty()) {
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("kill");
        command.add("-9");
        for (Integer each : toKill) {
            command.add(String.valueOf(each));
        }
        run(command, null, null);
    }

    // ================= Windows =================

    private static List<ProcessDTO> listWindows() throws IOException, InterruptedException {
        // 制表符分隔一行一进程，避免引入JSON依赖。用[char]9拼接而非双引号字符串：
        // 脚本经ProcessBuilder传参时，内嵌双引号会被破坏，[char]9写法无双引号、无反引号，稳妥。
        // 不强制UTF-8输出（部分主机PowerShell为ConstrainedLanguage模式，无法设置OutputEncoding），
        // 按JVM默认字符集读取（与原实现一致）。
        String script = "Get-CimInstance Win32_Process | ForEach-Object { "
            + "($_.ProcessId, $_.ParentProcessId, $_.Path, $_.CommandLine) -join [char]9 }";
        List<String> lines =
            runAndReadLines(Arrays.asList("powershell.exe", "-NoProfile", "-Command", script), Charset.defaultCharset());

        List<ProcessDTO> result = new ArrayList<>();
        for (String line : lines) {
            // -1保留尾部空字段（path/commandLine可能为空）
            String[] parts = line.split("\t", -1);
            if (parts.length < 4) {
                continue;
            }
            Integer pid = parseIntOrNull(parts[0]);
            if (pid == null) {
                continue;
            }
            result.add(ProcessDTO.builder()
                .pid(pid)
                .ppid(parseIntOrNull(parts[1]))
                .path(StringUtils.trimToNull(parts[2]))
                .commandLine(StringUtils.trimToNull(parts[3]))
                .build());
        }
        return result;
    }

    // ================= Linux =================

    private static List<ProcessDTO> listLinux() {
        List<ProcessDTO> result = new ArrayList<>();
        File[] entries = new File("/proc").listFiles();
        if (entries == null) {
            return result;
        }
        for (File dir : entries) {
            if (!dir.isDirectory() || !StringUtils.isNumeric(dir.getName())) {
                continue;
            }
            int pid = Integer.parseInt(dir.getName());
            try {
                String path = null;
                try {
                    path = Files.readSymbolicLink(Paths.get("/proc", dir.getName(), "exe")).toString();
                } catch (IOException ignore) {
                    // 无权限或已退出，path留空，仍保留pid/ppid用于杀树计算
                }

                String commandLine = null;
                byte[] cmdlineBytes = Files.readAllBytes(Paths.get("/proc", dir.getName(), "cmdline"));
                if (cmdlineBytes.length > 0) {
                    // cmdline以NUL分隔参数，替换为空格
                    commandLine =
                        new String(cmdlineBytes, StandardCharsets.UTF_8).replace('\0', ' ').trim();
                }

                result.add(ProcessDTO.builder()
                    .pid(pid)
                    .ppid(readLinuxPpid(dir.getName()))
                    .path(StringUtils.trimToNull(path))
                    .commandLine(StringUtils.trimToNull(commandLine))
                    .build());
            } catch (IOException ignore) {
                // 进程在遍历间隙退出，跳过
            }
        }
        return result;
    }

    private static Integer readLinuxPpid(String pid) throws IOException {
        for (String line : Files.readAllLines(Paths.get("/proc", pid, "status"), StandardCharsets.UTF_8)) {
            if (line.startsWith("PPid:")) {
                return parseIntOrNull(line.substring("PPid:".length()).trim());
            }
        }
        return null;
    }

    // ================= macOS =================

    private static List<ProcessDTO> listMac() throws IOException, InterruptedException {
        // 每次调用最后一列为“剩余整行”，便于稳妥解析含空格的路径/命令行
        Map<Integer, ProcessDTO.ProcessDTOBuilder> byPid = new LinkedHashMap<>();

        // pid ppid comm(可执行路径)
        for (String line : runAndReadLines(Arrays.asList("ps", "-ax", "-o", "pid=,ppid=,comm="),
            StandardCharsets.UTF_8)) {
            String[] parts = line.trim().split("\\s+", 3);
            if (parts.length < 3) {
                continue;
            }
            Integer pid = parseIntOrNull(parts[0]);
            if (pid == null) {
                continue;
            }
            byPid.put(pid, ProcessDTO.builder()
                .pid(pid)
                .ppid(parseIntOrNull(parts[1]))
                .path(StringUtils.trimToNull(parts[2])));
        }

        // pid command(完整命令行)
        for (String line : runAndReadLines(Arrays.asList("ps", "-ax", "-o", "pid=,command="),
            StandardCharsets.UTF_8)) {
            String[] parts = line.trim().split("\\s+", 2);
            if (parts.length < 2) {
                continue;
            }
            Integer pid = parseIntOrNull(parts[0]);
            ProcessDTO.ProcessDTOBuilder builder = pid == null ? null : byPid.get(pid);
            if (builder != null) {
                builder.commandLine(StringUtils.trimToNull(parts[1]));
            }
        }

        return byPid.values().stream().map(ProcessDTO.ProcessDTOBuilder::build).collect(Collectors.toList());
    }

    // ================= 通用 =================

    /**
     * 当前JVM进程自身的pid（Java 8下经RuntimeMXBean获取）
     */
    public static int currentPid() {
        // Java 8下获取自身pid："pid@host"
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(name.substring(0, name.indexOf('@')));
    }

    private static Integer parseIntOrNull(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static List<String> runAndReadLines(List<String> command, Charset charset)
        throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        // stderr交给父进程，避免管道阻塞且不污染stdout解析
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = processBuilder.start();

        List<String> lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset))) {
            lines = reader.lines().collect(Collectors.toList());
        }
        process.waitFor();
        return lines;
    }

    private ProcessUtils() {}
}
