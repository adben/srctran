package com.srctran.backend.entity.user;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.srctran.backend.entity.common.EnumMarshaller;

@XmlRootElement(name = "user")
@DynamoDBTable(tableName = "users")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  private String username;
  private Type type = Type.USER;
  private String email;
  private String password;
  private Date creationTime;
  private Status status;

  public User() {
  }

  public User(String username) {
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

  @JsonIgnore
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

  @JsonIgnore
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

  public static enum Type {
    USER
  }

  public static enum Status {
    ACTIVE,
    CONFIRMED
  }

  public static class TypeMarshaller extends EnumMarshaller<Type> {
  }

  public static class StatusMarshaller extends EnumMarshaller<Status> {
  }
}
