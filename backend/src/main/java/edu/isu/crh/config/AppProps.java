package edu.isu.crh.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "crh")
public record AppProps(Jwt jwt, Legacy legacy, Webhook webhook) {
  public record Jwt(String secret, String issuer, long ttlMinutes) {}
  public record Legacy(String baseUrl) {}
  public record Webhook(String url) {}
}
