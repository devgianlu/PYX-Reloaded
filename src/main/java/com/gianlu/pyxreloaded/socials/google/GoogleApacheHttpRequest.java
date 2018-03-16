package com.gianlu.pyxreloaded.socials.google;

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.StreamingContent;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class GoogleApacheHttpRequest extends LowLevelHttpRequest {
    private final HttpClient httpClient;
    private final HttpRequestBase request;

    GoogleApacheHttpRequest(HttpClient httpClient, HttpRequestBase request) {
        this.httpClient = httpClient;
        this.request = request;
    }

    @Override
    public void addHeader(String name, String value) {
        request.addHeader(name, value);
    }

    @Override
    public void setTimeout(int connectTimeout, int readTimeout) {
        request.setConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(connectTimeout)
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .build());
    }

    @Override
    public LowLevelHttpResponse execute() throws IOException {
        if (getStreamingContent() != null) {
            ContentEntity entity = new ContentEntity(getContentLength(), getStreamingContent());
            entity.setContentEncoding(getContentEncoding());
            entity.setContentType(getContentType());
            ((HttpEntityEnclosingRequest) request).setEntity(entity);
        }

        return new GoogleApacheHttpResponse(request, httpClient.execute(request));
    }

    private static final class ContentEntity extends AbstractHttpEntity {
        private final long contentLength;
        private final StreamingContent streamingContent;

        ContentEntity(long contentLength, StreamingContent streamingContent) {
            this.contentLength = contentLength;
            this.streamingContent = Preconditions.checkNotNull(streamingContent);
        }

        @Override
        public InputStream getContent() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getContentLength() {
            return contentLength;
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public boolean isStreaming() {
            return true;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            if (contentLength != 0) {
                streamingContent.writeTo(out);
            }
        }
    }
}

