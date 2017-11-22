package com.lz.proxytestdemo.util;

/**
 * Created by Administrator on 2017/11/22.
 */

public class Check {

    public static boolean emptyString(String s){
        return (s == null) || s.trim().equals("");
    }

    public static boolean legalIP(String s){
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        return !emptyString(s) && s.matches(rexp);
    }
}
