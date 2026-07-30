package com.iteknical.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.FileUtils;

/**
 * 下载工具
 * <p>
 * 自开{@link HttpURLConnection}并流式落盘（大文件不占内存，父目录自动创建），证书校验走JDK默认信任库（开启）。
 * 显式设置非Java的User-Agent：部分CDN的WAF会拦截Java默认UA（{@code Java/x.x.x}）返回403，
 * 而{@link FileUtils#copyURLToFile}无法定制请求头，故不用它。仅跟随同协议重定向，下载源须为直链。
 * </p>
 *
 * @author Tony
 */
public class DownloadUtils {

    /** 建连超时（毫秒） */
    private static final int    CONNECT_TIMEOUT_MILLIS = 15_000;

    /**
     * 读超时（毫秒）
     * <p>
     * 为两次数据到达之间的最大等待，非整体传输时长；给足以容忍慢链路，避免大文件传输中途被误判超时。
     * </p>
     */
    private static final int    READ_TIMEOUT_MILLIS    = 60_000;

    /** 避开CDN对Java默认UA的拦截；用明确的应用标识即可，无需伪装浏览器 */
    private static final String USER_AGENT             = "iteknical-download-utils";

    /**
     * 下载url到targetFile（覆盖写；父目录不存在时自动创建）
     *
     * @param url        下载直链
     * @param targetFile 目标文件
     * @throws IOException 建连、非2xx响应、读取或写盘失败（含url格式错误）
     */
    public static void download(String url, File targetFile) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(READ_TIMEOUT_MILLIS);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        try (InputStream in = connection.getInputStream()) {
            // 非2xx时getInputStream直接抛IOException，交由调用方重试
            FileUtils.copyInputStreamToFile(in, targetFile);
        } finally {
            connection.disconnect();
        }
    }

    private DownloadUtils() {}
}
