package com.hxs.client;

import com.hxs.constant.URLConstant;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;

import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * HTTP Mock 测试辅助基类 — 提供通用的 mock HttpClient / EduSession 创建方法
 */
abstract class HttpMockSupport {

    protected static final Map<String, String> TEST_COOKIES = Map.of(
            "jw", "test-jw", "JSESSIONID", "test-session"
    );

    static CloseableHttpClient mockHttpClient(int statusCode, String responseBody) throws IOException {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);

        StatusLine statusLine = new BasicStatusLine(
                new ProtocolVersion("HTTP", 1, 1), statusCode, "OK");
        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(new StringEntity(responseBody, ContentType.APPLICATION_JSON));

        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
        return httpClient;
    }

    static CloseableHttpClient mockHttpClientSequence(int statusCode, String... responseBodies) throws IOException {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse[] responses = new CloseableHttpResponse[responseBodies.length];

        for (int i = 0; i < responseBodies.length; i++) {
            responses[i] = mock(CloseableHttpResponse.class);
            StatusLine statusLine = new BasicStatusLine(
                    new ProtocolVersion("HTTP", 1, 1), statusCode, "OK");
            when(responses[i].getStatusLine()).thenReturn(statusLine);
            when(responses[i].getEntity())
                    .thenReturn(new StringEntity(responseBodies[i], ContentType.APPLICATION_JSON));
        }
        when(httpClient.execute(any(HttpUriRequest.class)))
                .thenReturn(responses[0], java.util.Arrays.copyOfRange(responses, 1, responses.length));
        return httpClient;
    }

    static CloseableHttpClient mockHttpClientError(int statusCode) throws IOException {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        StatusLine statusLine = new BasicStatusLine(
                new ProtocolVersion("HTTP", 1, 1), statusCode, "Error");
        when(response.getStatusLine()).thenReturn(statusLine);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
        return httpClient;
    }

    static CloseableHttpClient mockHttpClientException() throws IOException {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        when(httpClient.execute(any(HttpUriRequest.class))).thenThrow(new IOException("Connection refused"));
        return httpClient;
    }

    static EduSession spySession(CloseableHttpClient mockHttpClient) {
        EduSession realSession = new EduSession(TEST_COOKIES, URLConstant.BASE_URL);
        EduSession spy = spy(realSession);
        when(spy.getHttpClient()).thenReturn(mockHttpClient);
        return spy;
    }

    /** 匹配 POST 请求中包含指定 URI 片段的 Matcher */
    static HttpUriRequest uriContains(String fragment) {
        return argThat(req -> req.getURI().toString().contains(fragment));
    }
}
