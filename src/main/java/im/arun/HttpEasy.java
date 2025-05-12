package im.arun;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

/**
 * A fluent interface for making HTTP requests in Java
 */
public class HttpEasy {
    private final HttpClient client;
    private final Map<String, String> headers = new HashMap<>();
    private String url;
    private String body;
    private String method = "GET";
    private Map<String, String> formData = new HashMap<>();
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration requestTimeout = Duration.ofSeconds(10);
    private boolean followRedirects = true;

    /**
     * Constructs a new HttpEasy instance with default settings
     */
    public HttpEasy() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Creates a new HttpEasy instance
     * @return a new HttpEasy instance
     */
    public static HttpEasy create() {
        return new HttpEasy();
    }

    /**
     * Set URL for the request
     * @param url The URL to send the request to
     * @return this HttpEasy instance
     */
    public HttpEasy url(String url) {
        this.url = url;
        return this;
    }

    /**
     * Add a custom header
     * @param key The header name
     * @param value The header value
     * @return this HttpEasy instance
     */
    public HttpEasy header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    /**
     * Set Content-Type to application/json
     * @return this HttpEasy instance
     */
    public HttpEasy json() {
        this.header("Content-Type", "application/json");
        return this;
    }

    /**
     * Set request body for POST/PUT requests
     * @param body The request body as a string
     * @return this HttpEasy instance
     */
    public HttpEasy body(String body) {
        this.body = body;
        return this;
    }

    /**
     * Set the HTTP method (GET, POST, PUT, DELETE)
     * @param method The HTTP method to use
     * @return this HttpEasy instance
     */
    public HttpEasy method(String method) {
        this.method = method.toUpperCase();
        return this;
    }

    /**
     * Set Content-Type to application/x-www-form-urlencoded
     * @return this HttpEasy instance
     */
    public HttpEasy formdata() {
        this.header("Content-Type", "application/x-www-form-urlencoded");
        return this;
    }

    /**
     * Add a form field to the request
     * @param name The field name
     * @param value The field value
     * @return this HttpEasy instance
     */
    public HttpEasy form(String name, String value) {
        this.formData.put(name, value);
        return this;
    }

    /**
     * Set connection timeout
     * @param seconds Timeout in seconds
     * @return this HttpEasy instance
     */
    public HttpEasy connectTimeout(long seconds) {
        this.connectTimeout = Duration.ofSeconds(seconds);
        return this;
    }

    /**
     * Set request timeout
     * @param seconds Timeout in seconds
     * @return this HttpEasy instance
     */
    public HttpEasy requestTimeout(long seconds) {
        this.requestTimeout = Duration.ofSeconds(seconds);
        return this;
    }

    /**
     * Configure whether to follow redirects
     * @param follow true to follow redirects, false otherwise
     * @return this HttpEasy instance
     */
    public HttpEasy followRedirects(boolean follow) {
        this.followRedirects = follow;
        return this;
    }

    /**
     * Shortcut for setting the method to POST
     * @return this HttpEasy instance
     */
    public HttpEasy post() {
        return method("POST");
    }

    /**
     * Shortcut for setting the method to PUT
     * @return this HttpEasy instance
     */
    public HttpEasy put() {
        return method("PUT");
    }

    /**
     * Shortcut for setting the method to DELETE
     * @return this HttpEasy instance
     */
    public HttpEasy delete() {
        return method("DELETE");
    }

    /**
     * Send the HTTP request
     * @return A wrapper around the HTTP response
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws IllegalStateException If the URL is not set
     */
    public HttpResponseWrapper send() throws IOException, InterruptedException {
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("URL must be set before sending a request");
        }

        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .connectTimeout(connectTimeout);

        if (followRedirects) {
            clientBuilder.followRedirects(HttpClient.Redirect.NORMAL);
        } else {
            clientBuilder.followRedirects(HttpClient.Redirect.NEVER);
        }

        HttpClient client = clientBuilder.build();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(requestTimeout);

        // Apply headers
        for (var entry : headers.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }

        // Prepare the form body if Content-Type is application/x-www-form-urlencoded
        if (!formData.isEmpty()) {
            String formBody = formData.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" +
                            URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            builder.method("POST", HttpRequest.BodyPublishers.ofString(formBody));
        } else if (body != null) {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        try {
            HttpRequest request = builder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new HttpResponseWrapper(response);
        } catch (IOException e) {
            throw new IOException("Error sending HTTP request: " + e.getMessage(), e);
        }
    }

    /**
     * Utility class to wrap the HTTP response
     */
    public static class HttpResponseWrapper {
        private final int statusCode;
        private final String body;
        private final Map<String, List<String>> headers;

        public HttpResponseWrapper(HttpResponse<String> response) {
            this.statusCode = response.statusCode();
            this.body = response.body();
            this.headers = response.headers().map();
        }

        /**
         * Get the response status code
         * @return the status code
         */
        public int status() {
            return statusCode;
        }

        /**
         * Get the response body as a string
         * @return the response body
         */
        public String body() {
            return body;
        }

        /**
         * Check if the status code indicates success (2xx)
         * @return true if successful, false otherwise
         */
        public boolean isOk() {
            return statusCode >= 200 && statusCode < 300;
        }

        /**
         * Get all response headers
         * @return Map of header names to values
         */
        public Map<String, List<String>> headers() {
            return headers;
        }

        /**
         * Get a specific header value
         * @param name The header name
         * @return The header value, or null if not present
         */
        public String header(String name) {
            List<String> values = headers.get(name);
            return values != null && !values.isEmpty() ? values.get(0) : null;
        }

        /**
         * Parse the response body as JSON (requires a JSON library)
         * @return The parsed JSON object as a string (placeholder for actual JSON parsing)
         * @throws UnsupportedOperationException if the response is not valid JSON
         */
        public String json() {
            // In a real implementation, you would parse the body as JSON
            // For example, using Jackson or Gson
            // This is a placeholder that should be replaced with actual JSON parsing
            if (body == null || !body.trim().startsWith("{") && !body.trim().startsWith("[")) {
                throw new UnsupportedOperationException("Response body is not valid JSON");
            }
            return body;
        }

        @Override
        public String toString() {
            return "Status: " + statusCode + "\nHeaders: " + headers + "\nBody:\n" + body;
        }
    }

//    /**
//     * Example usage of HttpEasy
//     */
//    public static void main(String[] args) {
//        try {
//            // GET request example
//            HttpResponseWrapper response = HttpEasy.create()
//                    .url("https://jsonplaceholder.typicode.com/posts/1")
//                    .send();
//
//            System.out.println("GET Example:");
//            System.out.println("Status: " + response.status());
//            System.out.println("Body: " + response.body());
//
//            // POST request example
//            response = HttpEasy.create()
//                    .url("https://jsonplaceholder.typicode.com/posts")
//                    .post()
//                    .json()
//                    .body("{\"title\":\"foo\",\"body\":\"bar\",\"userId\":1}")
//                    .send();
//
//            System.out.println("\nPOST Example:");
//            System.out.println("Status: " + response.status());
//            System.out.println("Body: " + response.body());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}