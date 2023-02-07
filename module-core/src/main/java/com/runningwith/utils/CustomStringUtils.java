package com.runningwith.utils;

import java.util.UUID;

public abstract class CustomStringUtils {
    public static final String RANDOM_STRING = "ed5f86a7e49864acc917d078816e29bc";

    public static String getRandomUUID() {
        return UUID.randomUUID().toString();
    }
}
