package com.iteknical.common.setting;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.apache.commons.io.FileUtils;

/**
 * @author Tony
 */
public class SettingUtils {

    public static <T> T get(Class<T> clazz) {
        String json;
        try {
            if (!new File(clazz.getSimpleName()).exists()) {
                return null;
            }

            json = FileUtils.readFileToString(new File(clazz.getSimpleName()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return JSON.parseObject(json, clazz);
    }

    public static synchronized <T> void save(T t) {
        String json = JSON.toJSONString(t, SerializerFeature.PrettyFormat);
        try {
            FileUtils.writeStringToFile(new File(t.getClass().getSimpleName()), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
