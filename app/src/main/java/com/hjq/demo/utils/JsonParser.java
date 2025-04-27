package com.hjq.demo.utils;


import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hjq.gson.factory.GsonFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonParser {
    public static Gson gson = GsonFactory.getSingletonGson();

    /**
     * json字符串转泛型列表
     *
     * @param jsonArray json字符串
     * @param <T>  泛型
     * @return 泛型列表
     */
    public static <T> List<T> getListFromJson(String jsonArray, Class<T> clazz) {
        Type listType = TypeToken.getParameterized(List.class, clazz).getType();
        try {
            return gson.fromJson(jsonArray, listType);
        } catch (JsonSyntaxException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 返回json转换后的对象.
     */
//	List<Person> ps = gson.fromJson(str, new TypeToken<List<Person>>(){}.getType());
    public static <T> T deserializeByJson(String data, Type type) {
        return gson.fromJson(data, type);
    }

    /**
     * 返回json转换后的对象. 捕获异常
     */
    public static <T> T deserializeByJson(String data, Class<T> clz) {
        try {
            T t = gson.fromJson(data, clz);
            return t;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 返回json字符�?
     */
    public static <T> String serializeToJson(T t) {
        return gson.toJson(t);
    }

    public static Map getMapByJson(String data) {
        Map<String, Object> map = null;
        try {
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            map = gson.fromJson(data, type);
        } catch (JsonSyntaxException e) {
        }
        return map;
    }

    /**
     * 从assets中获取json文件
     */
    public static String getJson(Context context, String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}