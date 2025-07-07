package com.iteknical.common.utils;

/**
 * @Created by muliang.ykk.
 * @Date: 2017/11/22.
 * @Time: 上午11:30.
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * @author Tony
 */
public class HttpUtils {
    /**
     * urlEncode编码
     */
    private static final String ENCODE = "utf-8";

    private static final CloseableHttpClient HTTP_CLIENT;

    static {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();

        // for proxy debug
        // HttpHost proxy = new HttpHost("localhost", 8888);
        // RequestConfig defaultRequestConfig =
        // RequestConfig.custom().setProxy(proxy).setSocketTimeout(5000).setConnectTimeout(5000)
        // .setConnectionRequestTimeout(5000).build();

        RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setSocketTimeout(15000)
            .setConnectTimeout(15000)
            .setConnectionRequestTimeout(15000)
            .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(500);
        cm.setDefaultMaxPerRoute(250);
        HTTP_CLIENT = HttpClients.custom()
            .setConnectionManager(cm)
            .setDefaultRequestConfig(defaultRequestConfig)
            .build();
    }

    /**
     * get
     *
     * @param host
     * @param path
     * @param headers
     * @param queries
     * @return
     * @throws Exception
     */
    public static HttpResponse doGet(String host, String path, Map<String, String> headers,
        Map<String, String> queries) {
        HttpGet request = new HttpGet(buildUrl(host, path, queries));
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        try {
            return HTTP_CLIENT.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";

    /**
     * Post form
     *
     * @param host
     * @param path
     * @param headers
     * @param queries
     * @param bodies
     * @return
     * @throws Exception
     */
    public static HttpResponse doPost(String host, String path, Map<String, String> headers,
        Map<String, String> queries, Map<String, String> bodies) {
        HttpPost request = new HttpPost(buildUrl(host, path, queries));
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        if (MapUtils.isNotEmpty(bodies)) {
            List<NameValuePair> nameValuePairList = Lists.newArrayList();
            for (String key : bodies.keySet()) {
                nameValuePairList.add(new BasicNameValuePair(key, bodies.get(key)));
            }
            UrlEncodedFormEntity formEntity;
            try {
                formEntity = new UrlEncodedFormEntity(nameValuePairList, ENCODE);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            formEntity.setContentType(FORM_CONTENT_TYPE);
            request.setEntity(formEntity);
        }
        try {
            return HTTP_CLIENT.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Post String
     *
     * @param host
     * @param path
     * @param headers
     * @param queries
     * @param body
     * @return
     * @throws Exception
     */
    public static HttpResponse doPost(String host, String path, Map<String, String> headers,
        Map<String, String> queries, String body) {
        HttpPost request = new HttpPost(buildUrl(host, path, queries));
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        if (StringUtils.isNotBlank(body)) {
            request.setEntity(new StringEntity(body, ENCODE));

        }
        try {
            return HTTP_CLIENT.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Post stream
     *
     * @param host
     * @param path
     * @param headers
     * @param queries
     * @param body
     * @return
     * @throws Exception
     */
    public static HttpResponse doPost(String host, String path, Map<String, String> headers,
        Map<String, String> queries, byte[] body) {
        HttpPost request = new HttpPost(buildUrl(host, path, queries));
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        if (body != null) {
            request.setEntity(new ByteArrayEntity(body));
        }
        try {
            return HTTP_CLIENT.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildUrl(String host, String path, Map<String, String> queries) {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(host);

        if (StringUtils.isNotBlank(path)) {
            sbUrl.append(path);
        }

        if (MapUtils.isNotEmpty(queries)) {
            StringBuilder sbQuery = new StringBuilder();
            for (Map.Entry<String, String> query : queries.entrySet()) {
                if (0 < sbQuery.length()) {
                    sbQuery.append("&");
                }
                if (StringUtils.isBlank(query.getKey()) && StringUtils.isNotBlank(query.getValue())) {
                    sbQuery.append(query.getValue());
                }
                if (StringUtils.isNotBlank(query.getKey())) {
                    sbQuery.append(query.getKey());
                    if (StringUtils.isNotBlank(query.getValue())) {
                        sbQuery.append("=");
                        try {
                            sbQuery.append(URLEncoder.encode(query.getValue(), ENCODE));
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            if (0 < sbQuery.length()) {
                sbUrl.append("?").append(sbQuery);
            }
        }

        return sbUrl.toString();
    }

    public static String checkResponseAndGetResult(HttpResponse httpResponse, List<Integer> statusList) {
        if (httpResponse == null) {
            throw new RuntimeException();
        }
        if (httpResponse.getStatusLine() == null) {
            throw new RuntimeException();
        }
        if (!statusList.contains(httpResponse.getStatusLine().getStatusCode())) {
            throw new RuntimeException();
        }

        HttpEntity entity = httpResponse.getEntity();
        try {
            return EntityUtils.toString(entity, ENCODE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String checkResponseAndGetResult(HttpResponse httpResponse) {
        return checkResponseAndGetResult(httpResponse, ImmutableList.of(HttpStatus.SC_OK));
    }
}
