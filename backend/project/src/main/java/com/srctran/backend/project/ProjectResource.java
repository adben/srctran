package com.srctran.backend.project;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.srctran.backend.project.data.Project;

@Singleton
@Path("project")
public class ProjectResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Project getHello() {
    Project project = new Project();
    project.setName("Hello");
    return project;
  }
}
