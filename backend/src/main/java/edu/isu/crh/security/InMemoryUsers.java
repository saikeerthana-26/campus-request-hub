package edu.isu.crh.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

public class InMemoryUsers implements UserDetailsService {
  private final Map<String, UserDetails> users;

  public InMemoryUsers(PasswordEncoder encoder) {
    String pw = encoder.encode("Password123!");
    users = Map.of(
        "employee", User.withUsername("employee").password(pw).roles("EMPLOYEE").build(),
        "approver", User.withUsername("approver").password(pw).roles("APPROVER").build(),
        "admin", User.withUsername("admin").password(pw).roles("ADMIN").build()
    );
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    UserDetails u = users.get(username);
    if (u == null) throw new RuntimeException("User not found");
    return u;
  }
}
