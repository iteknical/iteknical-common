package com.iteknical.common.utils;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

import com.google.common.collect.Maps;

/**
 * @author Tony
 */
public class RestUtils {
    private static final String ENCODE = "utf-8";

    public static String doGet(String host, String path, Map<String, String> headers,
        Map<String, String> queries) {
        HttpResponse httpResponse = HttpUtils.doGet(host, path, headers, queries);
        return checkResponseAndGetResult(httpResponse);
    }

    public static String doPost(String host, String path, Map<String, String> headers,
        Map<String, String> queries, String body) {
        if (MapUtils.isEmpty(headers)) {
            headers = Maps.newHashMap();
        }
        headers.put("Content-Type", "application/json");
        HttpResponse httpResponse = HttpUtils.doPost(host, path, headers, queries, body);
        return checkResponseAndGetResult(httpResponse);
    }

    private static String checkResponseAndGetResult(HttpResponse httpResponse) {
        if (httpResponse == null) {
            throw new RuntimeException();
        }
        if (httpResponse.getStatusLine() == null) {
            throw new RuntimeException();
        }
        if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
            throw new RuntimeException();
        }

        HttpEntity entity = httpResponse.getEntity();
        try {
            return EntityUtils.toString(entity, ENCODE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
