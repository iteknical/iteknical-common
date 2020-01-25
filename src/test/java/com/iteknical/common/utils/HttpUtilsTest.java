package com.iteknical.common.utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class HttpUtilsTest {
    @Test
    public void test1Get() throws Exception {
        HttpResponse httpResponse = HttpUtils.doGet("http://www.baidu.com", null, null, null);
        Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        HttpEntity entity = httpResponse.getEntity();
        String responseString = EntityUtils.toString(entity, "utf-8");
        Assert.assertNotNull(responseString);
    }

    // @Test
    public void test2Get() throws Exception {
        HttpResponse httpResponse = HttpUtils.doGet("http://localhost:8081", "/fusion-user/api/checkUserMark", null,
            ImmutableMap.of("userMark", "15696756582@163.com"));
        Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        HttpEntity entity = httpResponse.getEntity();
        String responseString = EntityUtils.toString(entity, "utf-8");
        Assert.assertNotNull(responseString);
    }

    // @Test
    public void test3Post() throws Exception {
        HttpResponse httpResponse = HttpUtils.doPost("http://localhost:8081", "/fusion-user/api/login",
            ImmutableMap.of("Content-Type", "application/json"),
            null, "{\"userMark\":\"15696756584\",\"password\":\"22222222\",\"site\":\"wednesday\"}");
        Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        HttpEntity entity = httpResponse.getEntity();
        String responseString = EntityUtils.toString(entity, "utf-8");
        Assert.assertNotNull(responseString);
        Header[] headers = httpResponse.getHeaders("Set-Cookie");
        for (Header h : headers) {
            Assert.assertNotNull(h.getValue());
        }
    }
}
