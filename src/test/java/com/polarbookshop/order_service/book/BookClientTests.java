package com.polarbookshop.order_service.book;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockWebServer;

public class BookClientTests {

    private MockWebServer mockWebServer;
    private BookClient bookClient;

    @BeforeEach
    void setup() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
        var webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").uri().toString())
                .build();
        this.bookClient = new BookClient(webClient);
    }

    @AfterEach
    void clean() throws IOException {
        this.mockWebServer.shutdown();
    }
}
