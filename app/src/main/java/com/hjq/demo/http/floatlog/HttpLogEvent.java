package com.hjq.demo.http.floatlog;

import java.util.Objects;

/**
 * =============================
 * 作    者：r
 * 描    述：
 * 创建日期：2020/9/2 下午4:58
 * =============================
 */
public class HttpLogEvent {
    public String header;
    public String url;
    public String params;
    public int hashValue;
    public String results;

    public HttpLogEvent(String url, String par, int hashValue, String results, String header) {
        this.url = url;
        this.params = par;
        this.hashValue = hashValue;
        this.results = results;
        this.header = header;
    }

    public HttpLogEvent(String url, String par, int hashValue, String results) {
        this.url = url;
        this.params = par;
        this.hashValue = hashValue;
        this.results = results;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpLogEvent that = (HttpLogEvent) o;
        return Objects.equals(hashValue, that.hashValue);
    }

    @Override
    public int hashCode() {
        return hashValue;
    }

    public boolean isTrace() {
        return "Trace日志".equals(url);
    }
}
