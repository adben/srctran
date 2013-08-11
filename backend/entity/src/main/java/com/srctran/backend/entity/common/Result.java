package com.srctran.backend.entity.common;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user")
public class Result {

  private String message;

  public Result(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
