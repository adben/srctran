package com.srctran.backend.entity.common;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class Result {

  public static final Response MISSING_NEW_PASSWORD =
      Response.status(Status.BAD_REQUEST).entity(new Result("Missing new password")).build();
  public static final Response MISSING_OLD_PASSWORD =
      Response.status(Status.BAD_REQUEST).entity(new Result("Missing old password")).build();
  public static final Response SUCCESS = Response.ok().entity(new Result("Success")).build();
  public static final Response UNAUTHORIZED =
      Response.status(Status.UNAUTHORIZED).entity(new Result("Not authorized")).build();
  public static final Response WRONG_PASSWORD =
      Response.status(Status.BAD_REQUEST).entity(new Result("Wrong password")).build();

  private String message;

  public Result(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
