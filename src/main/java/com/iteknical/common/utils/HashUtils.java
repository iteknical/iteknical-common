package com.iteknical.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.google.common.hash.Hashing;

/**
 * @author Tony
 */
public class HashUtils {

    public static String sha512(String plain) {
        return Hashing.sha512().hashString(plain, StandardCharsets.UTF_8).toString();
    }

    public static String randomHex32() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
