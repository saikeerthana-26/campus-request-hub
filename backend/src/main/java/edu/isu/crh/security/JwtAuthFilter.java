package edu.isu.crh.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserDetailsService users;

  public JwtAuthFilter(JwtService jwtService, UserDetailsService users) {
    this.jwtService = jwtService;
    this.users = users;
  }
  @Bean
SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
  http
    .csrf(csrf -> csrf.disable())
    .cors(Customizer.withDefaults())
    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(auth -> auth
      .requestMatchers("/actuator/health").permitAll()
      .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
      .requestMatchers("/api/auth/**").permitAll()
      .anyRequest().authenticated()
    )
    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

  return http.build();
}


  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain chain) throws ServletException, IOException {

    String auth = request.getHeader("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    String token = auth.substring("Bearer ".length()).trim();
    try {
      Claims claims = jwtService.parse(token);
      String username = claims.getSubject();

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = users.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    } catch (Exception ignored) {
      // invalid token -> let security handle by rejecting later
    }

    chain.doFilter(request, response);
  }
}
