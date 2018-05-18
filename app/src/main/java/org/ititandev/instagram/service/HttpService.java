package org.ititandev.instagram.service;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.entity.ByteArrayEntity;

public class HttpService {

//    private static final String BASE_URL = "http://vre.hcmut.edu.vn/instagram";
    private static final String BASE_URL = "http://192.168.100.14:8081";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.removeHeader("Authorization");
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void getHeader(String url, String token, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", token);
        ByteArrayEntity requestText = new ByteArrayEntity("".getBytes());
        client.get(null, getAbsoluteUrl(url), requestText, "application/json", responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.removeHeader("Authorization");
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void postBody(String url, String requestBody, AsyncHttpResponseHandler responseHandler) {
        client.removeHeader("Authorization");
        ByteArrayEntity requestText = new ByteArrayEntity(requestBody.getBytes());
        client.post(null, getAbsoluteUrl(url), requestText, "application/json", responseHandler);
    }

    public static void postHeader(String url, String token, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", token);
        ByteArrayEntity requestText = new ByteArrayEntity("".getBytes());
        client.post(null, getAbsoluteUrl(url), requestText, "application/json", responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
