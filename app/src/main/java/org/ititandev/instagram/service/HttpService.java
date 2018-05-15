package org.ititandev.instagram.service;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.entity.ByteArrayEntity;

public class HttpService {

    private static final String BASE_URL = "http://vre.hcmut.edu.vn/instagram/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post_text(String url, String requestBody, AsyncHttpResponseHandler responseHandler) {
        ByteArrayEntity requestText = new ByteArrayEntity(requestBody.getBytes());
        client.post(null, getAbsoluteUrl(url), requestText, "application/json", responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
