package com.polarbookshop.order_service.order.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.polarbookshop.order_service.config.DataConfig;

import reactor.test.StepVerifier;

@DataR2dbcTest // R2DBC 컴포넌트에 집중하는 테스트 클래스임을 나타낸다.
@Import(DataConfig.class) // 감사(Auditing)를 활성화하기 위한 R2DBC 설정을 임포트한다.
@Testcontainers // 테스트컨테이너의 자동 시작과 중지를 활성화한다.
public class OrderRepositoryR2dbcTests {

    @Container // 테스트를 위한 PostgreSQL 컨테이너를 식별한다.
    static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.3"));

    @Autowired
    private OrderRepository orderRepository;

    @DynamicPropertySource // 테스트 PostgreSQL 인스턴스에 연결하도록 R2DBC와 플라이웨이 설정을 변경한다.
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", OrderRepositoryR2dbcTests::r2dbcUrl);
        registry.add("spring.r2dbc.username", postgresql::getUsername);
        registry.add("spring.r2dbc.password", postgresql::getPassword);
        registry.add("spring.flyway.url", postgresql::getJdbcUrl);
    }

    private static String r2dbcUrl() {
        return String.format("r2dbc:postgresql://%s:%s/%s",
                postgresql.getHost(),
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgresql.getDatabaseName());
    }

    @Test
    void createRejectedOrder() {
        var rejectedOrder = OrderService.buildRejectedOrder("123456780", 3);
        StepVerifier.create(orderRepository.save(rejectedOrder)) // StepVerifier 객체를 OrderRepository가 반환하는 객체로 초기화한다.
                .expectNextMatches(order -> order.status().equals(OrderStatus.REJECTED)) // 반환된 객체가 올바른 상태를 가지고 있는지 확인한다.
                .verifyComplete(); // 리액티브 스트림이 성공적으로 완료됐는지 확인한다.
    }
}
