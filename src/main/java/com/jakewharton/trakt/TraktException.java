package com.jakewharton.trakt;

import java.util.Map;
import com.jakewharton.apibuilder.ApiException;
import com.jakewharton.trakt.entities.Response;

public final class TraktException extends RuntimeException {
    private static final long serialVersionUID = 6158978902757706299L;

    private final String url;
    private final Map<String, Object> postBody;
    private final Response response;

    public TraktException(String url, ApiException cause) {
        this(url, null, cause);
    }
    public TraktException(String url, Map<String, Object> postBody, ApiException cause) {
        super(cause);
        this.url = url;
        this.postBody = postBody;
        this.response = null;
    }
    public TraktException(String url, Map<String, Object> postBody, ApiException cause, Response response) {
        super(response.error, cause);
        this.url = url;
        this.postBody = postBody;
        this.response = response;
    }

    public String getUrl() {
        return this.url;
    }
    public Map<String, Object> getPostBody() {
        return this.postBody;
    }
    public Response getResponse() {
        return this.response;
    }
}
