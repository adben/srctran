package com.srctran.backend.entity.project;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

@Singleton
@Path("project")
public class ProjectResource {

  @Context
  UriInfo uriInfo;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Project getHello() {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Project project = new Project();
    project.setName(user.getUsername());
    return project;
  }
}
