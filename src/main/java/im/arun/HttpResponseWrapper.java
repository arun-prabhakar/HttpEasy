package im.arun;

import java.net.http.HttpResponse;

// Utility class to wrap the HTTP response
public class HttpResponseWrapper {
    private final int statusCode;
    private final String body;

    public HttpResponseWrapper(HttpResponse<String> response) {
        this.statusCode = response.statusCode();
        this.body = response.body();
    }

    public int status() {
        return statusCode;
    }

    public String body() {
        return body;
    }

    public boolean isOk() {
        return statusCode >= 200 && statusCode < 300;
    }

    @Override
    public String toString() {
        return "Status: " + statusCode + "\nBody:\n" + body;
    }
}
