package com.srctran.backend.entity.project;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.srctran.backend.entity.common.Result;
import com.srctran.backend.entity.project.Project.ValidationError;

@Singleton
@Path("/project")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectResource {

  @Autowired
  private AmazonDynamoDBAsync db;

  @Autowired
  private DynamoDBMapper dbMapper;

  @POST
  @Path("~/create")
  public Response create(Project project) {
    ValidationError error = project.validate();
    if (error != null) {
      return Response.status(Status.BAD_REQUEST).entity(new Result(error.toString())).build();
    }
    return Result.SUCCESS;
  }
}
