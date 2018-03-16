package com.gianlu.pyxreloaded.socials.google;

import com.google.api.client.http.LowLevelHttpResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.io.InputStream;

final class GoogleApacheHttpResponse extends LowLevelHttpResponse {
    private final HttpRequestBase request;
    private final HttpResponse response;
    private final Header[] allHeaders;

    GoogleApacheHttpResponse(HttpRequestBase request, HttpResponse response) {
        this.request = request;
        this.response = response;
        allHeaders = response.getAllHeaders();
    }

    @Override
    public int getStatusCode() {
        StatusLine statusLine = response.getStatusLine();
        return statusLine == null ? 0 : statusLine.getStatusCode();
    }

    @Override
    public InputStream getContent() throws IOException {
        HttpEntity entity = response.getEntity();
        return entity == null ? null : entity.getContent();
    }

    @Override
    public String getContentEncoding() {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            Header contentEncodingHeader = entity.getContentEncoding();
            if (contentEncodingHeader != null) {
                return contentEncodingHeader.getValue();
            }
        }

        return null;
    }

    @Override
    public long getContentLength() {
        HttpEntity entity = response.getEntity();
        return entity == null ? -1 : entity.getContentLength();
    }

    @Override
    public String getContentType() {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            Header contentTypeHeader = entity.getContentType();
            if (contentTypeHeader != null) {
                return contentTypeHeader.getValue();
            }
        }

        return null;
    }

    @Override
    public String getReasonPhrase() {
        StatusLine statusLine = response.getStatusLine();
        return statusLine == null ? null : statusLine.getReasonPhrase();
    }

    @Override
    public String getStatusLine() {
        StatusLine statusLine = response.getStatusLine();
        return statusLine == null ? null : statusLine.toString();
    }

    @Override
    public int getHeaderCount() {
        return allHeaders.length;
    }

    @Override
    public String getHeaderName(int index) {
        return allHeaders[index].getName();
    }

    @Override
    public String getHeaderValue(int index) {
        return allHeaders[index].getValue();
    }

    @Override
    public void disconnect() {
        request.releaseConnection();
    }
}

