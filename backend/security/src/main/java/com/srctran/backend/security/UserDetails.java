package com.srctran.backend.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.srctran.backend.entity.user.User;

public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {

  public static final List<? extends GrantedAuthority> USER_AUTHORITIES =
      Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

  private static final long serialVersionUID = 1L;

  private User user;

  public UserDetails(User user) {
    this.user = user;
  }

  @Override
  public String getPassword() {
    return user == null ? null : user.getPassword();
  }

  @Override
  public String getUsername() {
    return user == null ? null : user.getUsername();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return USER_AUTHORITIES;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @JsonIgnore
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return user != null;
  }
}
