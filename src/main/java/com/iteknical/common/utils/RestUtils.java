package com.iteknical.common.utils;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.http.HttpResponse;

import com.google.common.collect.Maps;

/**
 * @author Tony
 */
public class RestUtils {
    public static String doPost(String host, String path, Map<String, String> headers,
        Map<String, String> queries, String body) {
        if (MapUtils.isEmpty(headers)) {
            headers = Maps.newHashMap();
        }
        headers.put("Content-Type", "application/json");
        HttpResponse httpResponse = HttpUtils.doPost(host, path, headers, queries, body);
        return HttpUtils.checkResponseAndGetResult(httpResponse);
    }

}
