package edu.isu.crh.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "crh")
public class AppProps {

  private Jwt jwt = new Jwt();
  private String legacyBaseUrl;
  private String webhookUrl;

  public Jwt getJwt() {
    return jwt;
  }

  public void setJwt(Jwt jwt) {
    this.jwt = jwt;
  }

  public String getLegacyBaseUrl() {
    return legacyBaseUrl;
  }

  public void setLegacyBaseUrl(String legacyBaseUrl) {
    this.legacyBaseUrl = legacyBaseUrl;
  }

  public String getWebhookUrl() {
    return webhookUrl;
  }

  public void setWebhookUrl(String webhookUrl) {
    this.webhookUrl = webhookUrl;
  }

  public static class Jwt {
    private String secret;
    private String issuer = "campus-request-hub";
    private long expiration = 14400000; // 4 hours default

    public String getSecret() {
      return secret;
    }

    public void setSecret(String secret) {
      this.secret = secret;
    }

    public String getIssuer() {
      return issuer;
    }

    public void setIssuer(String issuer) {
      this.issuer = issuer;
    }

    public long getExpiration() {
      return expiration;
    }

    public void setExpiration(long expiration) {
      this.expiration = expiration;
    }
  }
}
