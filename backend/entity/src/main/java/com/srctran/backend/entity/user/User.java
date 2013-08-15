package com.srctran.backend.entity.user;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.google.common.base.Strings;
import com.srctran.backend.entity.common.EnumMarshaller;

@XmlRootElement(name = "user")
@DynamoDBTable(tableName = "users")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z][a-z0-9._]+$");

  private String username;
  private Type type = Type.USER;
  private String email;
  private String password;
  private Date creationTime;
  private Status status;

  public User() {
  }

  public User(String username) {
    if (username != null) {
      username = username.toLowerCase();
    }
    this.username = username;
  }

  public User(String username, String email, String password, Status status) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.creationTime = new Date();
    this.status = status;
  }

  @DynamoDBHashKey
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @DynamoDBRangeKey
  @DynamoDBMarshalling(marshallerClass = TypeMarshaller.class)
  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Date getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(Date creationTime) {
    this.creationTime = creationTime;
  }

  @DynamoDBMarshalling(marshallerClass = StatusMarshaller.class)
  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public ValidationError validate() {
    // Username
    if (Strings.isNullOrEmpty(username)) {
      return ValidationError.MISSING_USERNAME;
    }
    if (username.length() < 3) {
      return ValidationError.USERNAME_TOO_SHORT;
    }
    if (username.length() > 10) {
      return ValidationError.USERNAME_TOO_LONG;
    }
    if (!USERNAME_PATTERN.matcher(username).matches()) {
      return ValidationError.USERNAME_CONTAINS_INVALID_CHARACTERS;
    }

    // Password
    if (Strings.isNullOrEmpty(password)) {
      return ValidationError.MISSING_PASSWORD;
    }
    if (password.length() < 4) {
      return ValidationError.PASSWORD_TOO_SHORT;
    }
    if (password.length() > 16) {
      return ValidationError.PASSWORD_TOO_LONG;
    }

    return null;
  }

  public static enum Type {
    USER
  }

  public static enum Status {
    ACTIVE,
    INITIALIZED
  }

  public static enum ValidationError {
    MISSING_USERNAME,
    USERNAME_TOO_LONG,
    USERNAME_TOO_SHORT,
    USERNAME_CONTAINS_INVALID_CHARACTERS,
    MISSING_PASSWORD,
    PASSWORD_TOO_LONG,
    PASSWORD_TOO_SHORT;

    @Override
    public String toString() {
      String name = name().replace('_', ' ');
      return name.charAt(0) + name().substring(1).toLowerCase();
    }
  }

  public static class TypeMarshaller extends EnumMarshaller<Type> {
  }

  public static class StatusMarshaller extends EnumMarshaller<Status> {
  }
}
