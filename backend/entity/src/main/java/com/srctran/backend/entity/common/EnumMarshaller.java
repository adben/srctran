package com.srctran.backend.entity.common;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

public class EnumMarshaller<T extends Enum<T>> implements DynamoDBMarshaller<T> {

  @Override
  public String marshall(T object) {
    return object == null ? null : object.name();
  }

  @Override
  public T unmarshall(Class<T> clazz, String value) {
    return value == null ? null : Enum.<T>valueOf(clazz, value);
  }
}
