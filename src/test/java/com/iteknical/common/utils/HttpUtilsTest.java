package com.iteknical.common.utils;

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

    @Test
    public void test2Get() throws Exception {
        HttpResponse httpResponse = HttpUtils.doGet("http://www.baidu.com", "/s", null, ImmutableMap.of("wd", "ip"));
        Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        HttpEntity entity = httpResponse.getEntity();
        String responseString = EntityUtils.toString(entity, "utf-8");
        Assert.assertNotNull(responseString);
    }
}
