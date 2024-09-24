package com.polarbookshop.order_service.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "polar")
public record ClientProperties(
        @NotNull URI catalogServiceUri) {

}
