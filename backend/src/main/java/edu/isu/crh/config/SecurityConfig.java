package edu.isu.crh.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import edu.isu.crh.security.JwtAuthFilter;
import edu.isu.crh.security.JwtService;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;





@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(AppProps.class)
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
@Bean
JwtAuthFilter jwtAuthFilter(JwtService jwtService, UserDetailsService uds) {
    return new JwtAuthFilter(jwtService, uds);
}


 

 @Bean
UserDetailsService userDetailsService(PasswordEncoder encoder) {
    String pw = encoder.encode("Password123!");

    UserDetails employee = User.withUsername("employee")
            .password(pw)
            .roles("EMPLOYEE")
            .build();

    UserDetails approver = User.withUsername("approver")
            .password(pw)
            .roles("APPROVER")
            .build();

    UserDetails admin = User.withUsername("admin")
            .password(pw)
            .roles("ADMIN")
            .build();

    return new InMemoryUserDetailsManager(employee, approver, admin);
}
@Bean
AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
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


}
