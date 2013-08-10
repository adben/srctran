package com.srctran.backend.entity.user;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;

public class DynamoUserDetailsService implements UserDetailsService {

  private DynamoDBMapper dbMapper;

  public void setDbMapper(DynamoDBMapper dbMapper) {
    this.dbMapper = dbMapper;
  }

  @Override
  public DynamoUserDetails loadUserByUsername(String username)
      throws UsernameNotFoundException {
    DynamoDBQueryExpression<DynamoUserDetails> query =
        new DynamoDBQueryExpression<DynamoUserDetails>().withHashKeyValues(new DynamoUserDetails(username));
    PaginatedQueryList<DynamoUserDetails> list = dbMapper.query(DynamoUserDetails.class, query);
    if (list.isEmpty()) {
      return null;
    } else {
      return list.get(0);
    }
  }
}
