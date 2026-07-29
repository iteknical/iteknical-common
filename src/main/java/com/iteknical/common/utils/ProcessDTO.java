package com.iteknical.common.utils;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * 进程信息
 *
 * @author Tony
 */
@Data
@Builder
public class ProcessDTO {
    /** 进程id */
    private Integer pid;
    /** 父进程id */
    private Integer ppid;
    /** 可执行文件全路径（部分系统进程可能为空） */
    private String  path;
    /** 命令行（部分系统进程可能为空） */
    private String  commandLine;

    @Tolerate
    public ProcessDTO() {}
}
