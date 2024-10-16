package com.polarbookshop.order_service.book;

import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class BookClient {

    private static final String BOOKS_ROOT_API = "/books/";
    private final WebClient webClient;

    public BookClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Book> getBookByIsbn(String isbn) {
        return webClient
                .get()
                .uri(BOOKS_ROOT_API + isbn)
                .retrieve()
                .bodyToMono(Book.class)
                // 뒤에 retryWhen이 왔으므로, 재시도에 대한 타임아웃이다.
                // -> 요청 하나 하나에 대한 타임아웃
                // retryWhen위에 timeout이 오면 전체(초기 요청부터 재시도까지) 작동에 대한 타임아웃이 된다.
                .timeout(Duration.ofSeconds(3), Mono.empty())
                // 404 오류는 빈 객체를 반환해 재시도를 하지 않게 한다.
                .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.empty())
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(100)) // 지수 백오프: 재시도 횟수가 늘어남에 따라 지연 시간도 늘어난다.
                )
                // 재시도 후 오류가 발생하면 빈 객체를 반환한다.
                .onErrorResume(Exception.class, exception -> Mono.empty());
    }

}
