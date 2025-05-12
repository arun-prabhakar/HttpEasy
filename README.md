# HttpEasy

A lightweight, fluent Java HTTP client library that simplifies making HTTP requests.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/java-11%2B-blue.svg)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)

## Features

- Fluent, chainable API design for clean and readable code
- Support for all common HTTP methods (GET, POST, PUT, DELETE, etc.)
- Easy handling of headers and content types
- Form data and JSON request support
- Configurable timeouts and redirect following
- Simple response handling with status checks and body access
- Built on the modern Java HTTP Client (introduced in Java 11)
- No external dependencies

## Installation

### Maven

```xml
<dependency>
    <groupId>com.github.arun-prabhakar</groupId>
    <artifactId>httpeasy</artifactId>
    <version>1.0.0</version>
</dependency>
```
#### For **Maven**, add this to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
### Gradle

```groovy
implementation 'com.github.arun-prabhakar:httpeasy:1.0.0'

repositories {
    maven { url 'https://jitpack.io' }
}
```
## Quick Start

```java
import com.github.arun-prabhakar.HttpEasy;

// Simple GET request
HttpEasy.create()
    .url("https://api.example.com/users")
    .send()
    .body();
```

## Examples

### GET Request

```java
import com.github.arun-prabhakar.HttpEasy;

HttpEasy.HttpResponseWrapper response = HttpEasy.create()
    .url("https://jsonplaceholder.typicode.com/posts/1")
    .send();

if (response.isOk()) {
    System.out.println("Response: " + response.body());
} else {
    System.out.println("Error: " + response.status());
}
```

### POST Request with JSON

```java
String jsonBody = "{\"title\":\"foo\",\"body\":\"bar\",\"userId\":1}";

HttpEasy.HttpResponseWrapper response = HttpEasy.create()
    .url("https://jsonplaceholder.typicode.com/posts")
    .post()           // Shorthand for .method("POST")
    .json()           // Sets Content-Type: application/json
    .body(jsonBody)
    .send();

System.out.println("Status code: " + response.status());
System.out.println("Response body: " + response.body());
```

### Form Data Submission

```java
HttpEasy.HttpResponseWrapper response = HttpEasy.create()
    .url("https://httpbin.org/post")
    .formdata()       // Sets Content-Type: application/x-www-form-urlencoded
    .form("username", "johndoe")
    .form("password", "secret")
    .post()
    .send();

System.out.println(response.body());
```

### Custom Headers

```java
HttpEasy.HttpResponseWrapper response = HttpEasy.create()
    .url("https://api.example.com/protected-resource")
    .header("Authorization", "Bearer your-token-here")
    .header("Accept", "application/json")
    .send();

System.out.println(response.body());
```

### Configure Timeouts

```java
HttpEasy.HttpResponseWrapper response = HttpEasy.create()
    .url("https://api.example.com/slow-operation")
    .connectTimeout(5)        // 5 seconds for connection establishment
    .requestTimeout(30)       // 30 seconds for the entire request
    .send();
```

### Disable Redirect Following

```java
HttpEasy.HttpResponseWrapper response = HttpEasy.create()
    .url("https://httpbin.org/redirect/1")
    .followRedirects(false)
    .send();

System.out.println("Status code: " + response.status());  // Will show 302
```

### Working with Response Headers

```java
HttpEasy.HttpResponseWrapper response = HttpEasy.create()
    .url("https://httpbin.org/response-headers?Content-Type=application/json")
    .send();

String contentType = response.header("Content-Type");
System.out.println("Content-Type: " + contentType);

// Get all headers
Map<String, List<String>> headers = response.headers();
headers.forEach((key, values) -> {
    System.out.println(key + ": " + String.join(", ", values));
});
```

### PUT Request

```java
String updateData = "{\"id\":1,\"title\":\"Updated title\",\"completed\":true}";

HttpEasy.HttpResponseWrapper response = HttpEasy.create()
    .url("https://jsonplaceholder.typicode.com/todos/1")
    .put()
    .json()
    .body(updateData)
    .send();

System.out.println("Update status: " + response.status());
```

### DELETE Request

```java
HttpEasy.HttpResponseWrapper response = HttpEasy.create()
    .url("https://jsonplaceholder.typicode.com/posts/1")
    .delete()
    .send();

System.out.println("Delete status: " + response.status());
```

### Complete Example with Error Handling

```java
import com.github.arun-prabhakar.HttpEasy;

import java.io.IOException;

public class Example {
    public static void main(String[] args) {
        try {
            // Create and configure the request
            HttpEasy.HttpResponseWrapper response = HttpEasy.create()
                .url("https://jsonplaceholder.typicode.com/posts")
                .header("User-Agent", "HttpEasy Client")
                .header("Accept-Language", "en-US,en;q=0.5")
                .connectTimeout(5)
                .requestTimeout(15)
                .post()
                .json()
                .body("{\"title\":\"HttpEasy Demo\",\"body\":\"This is a test\",\"userId\":1}")
                .send();
            
            // Handle the response
            if (response.isOk()) {
                System.out.println("Request successful!");
                System.out.println("Status: " + response.status());
                System.out.println("Body: " + response.body());
                
                // Access specific header
                String contentType = response.header("Content-Type");
                System.out.println("Content-Type: " + contentType);
            } else {
                System.err.println("Request failed with status: " + response.status());
                System.err.println("Error response: " + response.body());
            }
            
        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Request was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}
```

## Integration with JSON Libraries

HttpEasy is designed to work seamlessly with popular JSON libraries. Here's an example with Jackson:

```java
// Add Jackson to your dependencies
// implementation 'com.fasterxml.jackson.core:jackson-databind:2.13.0'

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.arun-prabhakar.HttpEasy;

// Define a POJO for your data
class User {
    private int id;
    private String name;
    private String email;
    
    // Getters and setters...
}

// Create ObjectMapper instance
ObjectMapper mapper = new ObjectMapper();

// Make request and parse JSON response
HttpEasy.HttpResponseWrapper response = HttpEasy.create()
    .url("https://jsonplaceholder.typicode.com/users/1")
    .send();

if (response.isOk()) {
    try {
        // Parse JSON to Java object
        User user = mapper.readValue(response.body(), User.class);
        System.out.println("User name: " + user.getName());
    } catch (Exception e) {
        System.err.println("Error parsing JSON: " + e.getMessage());
    }
}
```

## Requirements

- Java 11 or higher

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
