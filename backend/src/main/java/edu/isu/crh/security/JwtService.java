package edu.isu.crh.security;

import edu.isu.crh.config.AppProps;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {
  private final SecretKey key;
  private final String issuer;
  private final long ttlMillis;

  public JwtService(AppProps props) {
    String secret = props.getJwt().getSecret();
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("crh.jwt.secret must be set");
    }

    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.issuer = props.getJwt().getIssuer();
    this.ttlMillis = props.getJwt().getExpiration(); // milliseconds
  }

  public String createToken(String username, List<String> roles) {
    Instant now = Instant.now();
    Instant exp = now.plusMillis(ttlMillis);

    return Jwts.builder()
        .issuer(issuer)
        .subject(username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .claims(Map.of("roles", roles))
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
