package com.runningwith.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public abstract class CustomStringUtils {
    public static final String RANDOM_STRING = "ed5f86a7e49864acc917d078816e29bc";
    public static final String WITH_USER_NICKNAME = "a9cff1bc82a49d63bd7b0a42521b9172";

    public static String getRandomUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getEncodedUrl(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }
}
