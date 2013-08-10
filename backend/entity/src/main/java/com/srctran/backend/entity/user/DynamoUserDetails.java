package com.srctran.backend.entity.user;

import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.srctran.backend.entity.common.EnumMarshaller;

@DynamoDBTable(tableName = "users")
public class DynamoUserDetails implements UserDetails {

  private static final long serialVersionUID = 1L;

  private String username;
  private String email;
  private String password;
  private Status status;
  private Date createdDate;

  public DynamoUserDetails() {
  }

  protected DynamoUserDetails(String username) {
    this.username = username;
  }

  @DynamoDBHashKey
  @Override
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @DynamoDBMarshalling(marshallerClass = StatusMarshaller.class)
  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  @DynamoDBRangeKey
  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return null;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public static enum Status {
    ACTIVE,
    CONFIRMED
  }

  public static class StatusMarshaller extends EnumMarshaller<Status> {
  }
}
