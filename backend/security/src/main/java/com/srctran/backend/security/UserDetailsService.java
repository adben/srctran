package com.srctran.backend.security;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.srctran.backend.entity.user.User;
import com.srctran.backend.entity.user.UserResource;

public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

  private DynamoDBMapper dbMapper;

  public void setDbMapper(DynamoDBMapper dbMapper) {
    this.dbMapper = dbMapper;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = UserResource.loadUser(dbMapper, username);
    if (user == null) {
      throw new UsernameNotFoundException(String.format("User %s does not exist", username));
    }
    return new UserDetails(user);
  }
}
