package com.srctran.backend.server;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.AnnotationIntrospector.Pair;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.springframework.beans.factory.annotation.Autowired;

@Provider
public class JsonObjectMapperProvider implements ContextResolver<ObjectMapper> {

  @Autowired
  private TestBean testBean;

  private ObjectMapper objectMapper;

  public JsonObjectMapperProvider() {
    AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
    AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector();
    Pair introspectors = new Pair(jacksonIntrospector, jaxbIntrospector);
    objectMapper = new ObjectMapper();
    // objectMapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
    // objectMapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);
    SerializationConfig serializationConfig =
        objectMapper.getSerializationConfig().withAnnotationIntrospector(introspectors);
    objectMapper.setSerializationConfig(serializationConfig);
    DeserializationConfig deserializationConfig =
        objectMapper.getDeserializationConfig().withAnnotationIntrospector(introspectors);
    objectMapper.setDeserializationConfig(deserializationConfig);
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return objectMapper;
  }
}
