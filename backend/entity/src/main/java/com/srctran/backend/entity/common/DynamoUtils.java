package com.srctran.backend.entity.common;

import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.google.common.collect.ImmutableMap;

public class DynamoUtils {

  public static final Map<String, AttributeValue> primaryKey(String hashKey, String rangeKey) {
    return primaryKey("id", hashKey, "type", rangeKey);
  }

  public static final Map<String, AttributeValue> primaryKey(String hashKeyName, String hashKey,
      String rangeKeyName, String rangeKey) {
    return ImmutableMap.of(
        hashKeyName, new AttributeValue().withS(hashKey),
        rangeKeyName, new AttributeValue().withS(rangeKey));
  }

  public static final Map<String, ExpectedAttributeValue> expected(String attribute, String value) {
    return ImmutableMap.of(attribute,
        new ExpectedAttributeValue().withValue(new AttributeValue().withS(value)));
  }

  public static final Map<String, AttributeValueUpdate> update(String attribute, String value) {
    return ImmutableMap.of(attribute,
        new AttributeValueUpdate().withValue(new AttributeValue().withS(value)));
  }
}
