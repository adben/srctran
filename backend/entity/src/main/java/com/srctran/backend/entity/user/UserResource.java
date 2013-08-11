package com.srctran.backend.entity.user;

import java.util.Date;

import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.util.DateUtils;
import com.srctran.backend.entity.common.DynamoUtils;
import com.srctran.backend.entity.common.Result;

@Singleton
@Path("/user/{username}")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

  private DateUtils dateUtils = new DateUtils();

  @Autowired
  private AmazonDynamoDBAsync db;

  @Autowired
  private DynamoDBMapper dbMapper;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @GET
  public User getUser(@PathParam("username") String username) {
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    if ("~".equals(username) || currentUsername.equals(username)) {
      return getUserDetails(dbMapper, currentUsername);
    } else {
      return null;
    }
  }

  @POST
  public Response changePassword(@FormParam("oldPassword") String oldPassword,
      @FormParam("newPassword") String newPassword) {
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User userDetails = getUserDetails(dbMapper, currentUsername);
    if (passwordEncoder.matches(oldPassword, userDetails.getPassword())) {
      UpdateItemRequest request =
          new UpdateItemRequest().withTableName("users")
                                 .withKey(DynamoUtils.primaryKey(
                                     "username", currentUsername,
                                     "type", User.Type.USER.name()))
                                 .withExpected(DynamoUtils.expected(
                                     "password", userDetails.getPassword()))
                                 .withAttributeUpdates(DynamoUtils.update(
                                     "password", passwordEncoder.encode(newPassword)))
                                 .withReturnValues(ReturnValue.NONE);
      db.updateItem(request);
      return Response.ok().entity(new Result("Success")).build();
    } else {
      return Response.notModified().entity(new Result("Wrong old password")).build();
    }
  }

  public static User getUserDetails(DynamoDBMapper dbMapper, String username) {
    return dbMapper.load(new User(username));
  }

  private String getTimeString(Date time) {
    return dateUtils.formatIso8601Date(time);
  }
}
