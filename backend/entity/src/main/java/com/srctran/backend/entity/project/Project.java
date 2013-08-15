package com.srctran.backend.entity.project;

import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.google.common.base.Strings;
import com.srctran.backend.entity.common.EnumMarshaller;

@XmlRootElement(name = "project")
@DynamoDBTable(tableName = "project")
public class Project {

  private static final Pattern PROJECT_KEY_PATTERN = Pattern.compile("^[a-z][a-z0-9._]+$");

  private String key;
  private Type type;
  private String name;
  private String description;
  private Status status;

  @DynamoDBHashKey
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @DynamoDBRangeKey
  @DynamoDBMarshalling(marshallerClass = TypeMarshaller.class)
  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @DynamoDBMarshalling(marshallerClass = StatusMarshaller.class)
  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public ValidationError validate() {
    // Project key
    if (Strings.isNullOrEmpty(key)) {
      return ValidationError.MISSING_PROJECT_KEY;
    }
    if (key.length() < 3) {
      return ValidationError.PROJECT_KEY_TOO_SHORT;
    }
    if (key.length() > 16) {
      return ValidationError.PROJECT_KEY_TOO_LONG;
    }
    if (!PROJECT_KEY_PATTERN.matcher(key).matches()) {
      return ValidationError.PROJECT_KEY_CONTAINS_INVALID_CHARACTERS;
    }

    // Name
    if (name.length() < 3) {
      return ValidationError.PROJECT_NAME_TOO_SHORT;
    }
    if (name.length() > 24) {
      return ValidationError.PROJECT_NAME_TOO_LONG;
    }

    // Description
    if (description.length() > 2000) {
      return ValidationError.PROJECT_DESCRIPTION_TOO_LONG;
    }

    return null;
  }

  public static enum Type {
    GITHUB
  }

  public static enum Status {
    ACTIVE,
    INACTIVE
  }

  public static enum ValidationError {
    MISSING_PROJECT_KEY,
    PROJECT_KEY_TOO_LONG,
    PROJECT_KEY_TOO_SHORT,
    PROJECT_KEY_CONTAINS_INVALID_CHARACTERS,
    PROJECT_NAME_TOO_LONG,
    PROJECT_NAME_TOO_SHORT,
    PROJECT_DESCRIPTION_TOO_LONG;

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
