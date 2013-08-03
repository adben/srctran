package com.srctran.backend.project.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "project")
public class Project {

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
