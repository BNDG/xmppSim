package com.hjq.demo.http.floatlog;

import android.text.TextUtils;


import com.hjq.demo.BuildConfig;
import com.hjq.demo.utils.Trace;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;

public class LoggingInterceptor implements Interceptor {
    private final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    public Response intercept(Chain chain) throws IOException {

        // 拦截请求，获取到该次请求的request
        Request request = chain.request();

        String orginUrl = request.url().toString();
        if(!BuildConfig.LOG_ENABLE || orginUrl.contains("file") || orginUrl.contains("/myboot/pe/report")) {
            sendEvent(request, "", orginUrl);
            return chain.proceed(request);
        }
        RequestBody requestBody = request.body();
        String body = null;

        if (requestBody != null) {
            if(!canLog(requestBody.contentType())) {
                Trace.d("intercept: 是文件" + orginUrl);
                return chain.proceed(request);
            }
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            body = buffer.readString(charset);
        }

        sendEvent(request, body, null);
        // 执行本次网络请求操作，返回response信息
        Response response = chain.proceed(request);
        ResponseBody responseBody = response.body();
        String rBody = null;

        if (HttpHeaders.hasBody(response)) {
            if (!canLog(responseBody.contentType())) {
                Trace.d("intercept: 是文件 忽略结果");
                sendEvent(request, body, orginUrl);
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    try {
                        charset = contentType.charset(UTF8);
                    } catch (UnsupportedCharsetException e) {
                        e.printStackTrace();
                    }
                }
                rBody = buffer.clone().readString(charset);
                sendEvent(request, body, rBody);
            }
        }
        return response;
    }

    private void sendEvent(Request request, String body, String results) {
        results = results == null ? "" : results;
        String orginUrl = request.url().toString();
        String accessToken = request.header("accessToken");
        int normalIndex = orginUrl.indexOf("/myboot");
        String url = orginUrl;
        if (normalIndex != -1) {
            url = orginUrl.substring(normalIndex + "/myboot".length() + 1);
        }
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(body)) {
            Trace.d("sendEvent: body" + body);
            String par = "";
            if (!body.startsWith("{")) {
                par = "{" + "\r\n";
                String[] split = body.split("&");
                for (String sp : split) {
                    String[] map = sp.split("=");
                    String key = map[0];
                    String value = null;
                    if (map.length > 1) {
                        value = map[1];
                    }
                    value = TextUtils.isEmpty(value) ? "\"\"" : value;
                    par += "\"" + key + "\":" + value;
                    par += "\r\n";
                }
                par += "}";
            } else {
                par = body;
            }
            int hashValue = (url + body).hashCode();
            EventBus.getDefault().post(new HttpLogEvent(url, par, hashValue, results, accessToken));
        } else {
            int hashValue = url.hashCode();
            EventBus.getDefault().post(new HttpLogEvent(url, "", hashValue, results, accessToken));
        }
    }

    private boolean canLog(MediaType mediaType) {
        if (null != mediaType) {
            String mediaTypeString = mediaType.toString();
            if (!TextUtils.isEmpty(mediaTypeString)) {
                mediaTypeString = mediaTypeString.toLowerCase();
                return mediaTypeString.contains("text")
                        || mediaTypeString.contains("application/json")
                        || mediaTypeString.contains("x-www-form-urlencoded");
            }
        }
        return false;
    }
}