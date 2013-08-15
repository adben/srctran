package com.srctran.backend.entity.user;

import java.util.Collections;
import java.util.Date;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.util.DateUtils;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.srctran.backend.entity.common.DynamoUtils;
import com.srctran.backend.entity.common.Result;
import com.srctran.backend.entity.user.User.ValidationError;

@Singleton
@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

  private static final Logger LOG = Logger.getLogger(UserResource.class);

  private static final DateUtils DATE_UTILS = new DateUtils();

  @Autowired
  private AmazonDynamoDBAsync db;

  @Autowired
  private DynamoDBMapper dbMapper;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @GET
  @Path("{username}")
  public Response get(@PathParam("username") String username) {
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    if ("~".equals(username) || currentUsername.equals(username)) {
      User user = loadUser(dbMapper, currentUsername);
      user.setPassword("******");
      return Response.ok().entity(user).build();
    } else {
      return Result.UNAUTHORIZED;
    }
  }

  @POST
  @Path("~/create")
  public Response create(User user) {
    ValidationError error = user.validate();
    if (error != null) {
      return Response.status(Status.BAD_REQUEST).entity(new Result(error.toString())).build();
    }

    String username = user.getUsername();
    String email = user.getEmail();
    String password = passwordEncoder.encode(user.getPassword());
    String type = "USER";
    String status = User.Status.INITIALIZED.name();
    String creationTime = DATE_UTILS.formatIso8601Date(new Date());
    PutItemRequest request =
        new PutItemRequest().withTableName("users")
            .withItem(ImmutableMap.<String, AttributeValue>builder()
                                  .put("username", new AttributeValue().withS(username))
                                  .put("email", new AttributeValue().withS(email))
                                  .put("password", new AttributeValue().withS(password))
                                  .put("type", new AttributeValue().withS(type))
                                  .put("status", new AttributeValue().withS(status))
                                  .put("creationTime", new AttributeValue().withS(creationTime))
                                  .build())
            .withExpected(Collections.singletonMap("username",
                new ExpectedAttributeValue().withExists(false)));
    try {
      db.putItem(request);
    } catch (ConditionalCheckFailedException e) {
      return Response.status(Status.BAD_REQUEST)
                     .entity(new Result(String.format("User %s already exists", username)))
                     .build();
    }
    LOG.info(String.format("User %s created.", username));
    return Result.SUCCESS;
  }

  @POST
  @Path("{username}/password")
  public Response changePassword(@PathParam("username") String username,
      PasswordChangeBody body) {
    String oldPassword = body.getOldPassword();
    if (Strings.isNullOrEmpty(oldPassword)) {
      return Result.MISSING_OLD_PASSWORD;
    }
    String newPassword = body.getNewPassword();
    if (Strings.isNullOrEmpty(newPassword)) {
      return Result.MISSING_NEW_PASSWORD;
    }
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    if (!"~".equals(username) && !currentUsername.equals(username)) {
      return Result.UNAUTHORIZED;
    }

    User user = loadUser(dbMapper, currentUsername);
    if (passwordEncoder.matches(oldPassword, user.getPassword())) {
      UpdateItemRequest request =
          new UpdateItemRequest().withTableName("users")
                                 .withKey(DynamoUtils.primaryKey(
                                     "username", currentUsername,
                                     "type", User.Type.USER.name()))
                                 .withExpected(DynamoUtils.expected(
                                     "password", user.getPassword()))
                                 .withAttributeUpdates(DynamoUtils.update(
                                     "password", passwordEncoder.encode(newPassword)))
                                 .withReturnValues(ReturnValue.NONE);
      db.updateItem(request);
      return Result.SUCCESS;
    } else {
      return Result.WRONG_PASSWORD;
    }
  }

  public static User loadUser(DynamoDBMapper dbMapper, String username) {
    return dbMapper.load(new User(username));
  }

  @XmlRootElement
  public static class PasswordChangeBody {

    private String oldPassword;
    private String newPassword;

    public String getOldPassword() {
      return oldPassword;
    }

    public String getNewPassword() {
      return newPassword;
    }
  }
}
