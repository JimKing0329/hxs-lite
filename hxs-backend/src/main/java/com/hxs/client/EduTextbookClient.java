package com.hxs.client;

import com.alibaba.fastjson2.JSON;
import com.hxs.constant.MessageConstant;
import com.hxs.exception.RequestFailException;
import com.hxs.model.support.TextBookItem;
import com.hxs.model.support.TextBookResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.internal.StringUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * 教材模块爬虫 Client — 只负责教材信息爬取
 */
@Slf4j
public class EduTextbookClient {

    private final EduSession session;

    public EduTextbookClient(EduSession session) {
        this.session = session;
    }

    /**
     * 获取教材信息
     */
    public List<TextBookItem> getTextBook(String year, String term) {
        try {
            URI textBookURL = new URI(session.getBaseUrl())
                    .resolve("jcmxcx/jcmxcx_cxJcmxcxIndex.html?doType=query&gnmkdm=N759020");
            HttpPost httpPost = new HttpPost(textBookURL);
            httpPost.setHeader("Cookie", session.getCookieString());

            List<BasicNameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("queryType", "1"));
            formData.add(new BasicNameValuePair("xnm", year));
            formData.add(new BasicNameValuePair("xqm", term));
            formData.add(new BasicNameValuePair("queryModel.currentPage", "1"));
            formData.add(new BasicNameValuePair("queryModel.showCount", "100"));
            httpPost.setEntity(new UrlEncodedFormEntity(formData, "UTF-8"));

            try (CloseableHttpResponse response = session.getHttpClient().execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RequestFailException(MessageConstant.SYSTEM_ERROR);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                session.checkLogin(responseBody);

                List<TextBookItem> allBooks = JSON.parseObject(responseBody, TextBookResponse.class).getTextBooks();
                List<TextBookItem> books = allBooks.stream()
                        .filter(item -> !StringUtil.isBlank(item.getCourseName())
                                && !"无教材".equals(item.getBookName()))
                        .toList();
                log.debug("获取教材成功 {} 条", books.size());
                return books;
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取教材信息失败", e);
            throw new RequestFailException(MessageConstant.REQUEST_ERROR);
        }
    }
}
