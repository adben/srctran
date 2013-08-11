package com.srctran.backend.security;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.srctran.backend.entity.common.DynamoUtils;

public class DynamoTokenStore implements TokenStore {

  private static final Logger LOG = Logger.getLogger(DynamoTokenStore.class);

  private static final String DEFAULT_OAUTH_ACCESS_TOKEN_TABLE = "oauth_tokens";
  private static final String OAUTH2_TYPE = "OAUTH2";
  private static final String REFRESH_TYPE = "REFRESH";

  private AmazonDynamoDBClient db;
  private String oauthAccessTokenTable = DEFAULT_OAUTH_ACCESS_TOKEN_TABLE;
  private AuthenticationKeyGenerator authenticationKeyGenerator =
      new DefaultAuthenticationKeyGenerator();

  public void setDb(AmazonDynamoDBClient db) {
    this.db = db;
  }

  public void setOauthAccessTokenTable(String oauthAccessTokenTable) {
    this.oauthAccessTokenTable = oauthAccessTokenTable;
  }

  public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
    this.authenticationKeyGenerator = authenticationKeyGenerator;
  }

  @Override
  public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
    return readAuthentication(token.getValue());
  }

  @Override
  public OAuth2Authentication readAuthentication(String token) {
    GetItemRequest request =
        new GetItemRequest().withTableName(oauthAccessTokenTable)
                            .withKey(DynamoUtils.primaryKey(extractTokenKey(token), OAUTH2_TYPE))
                            .withAttributesToGet("authentication")
                            .withConsistentRead(true);
    GetItemResult result = db.getItem(request);
    if (result.getItem() == null) {
      return null;
    } else {
      ByteBuffer buffer = result.getItem().get("authentication").getB();
      return SerializationUtils.deserialize(buffer.array());
    }
  }

  @Override
  public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
    String tokenId = extractTokenKey(token.getValue());
    String authenticationId = authenticationKeyGenerator.extractKey(authentication);
    String username = authentication.isClientOnly() ? null : authentication.getName();
    String clientId = authentication.getAuthorizationRequest().getClientId();
    byte[] tokenData = SerializationUtils.serialize(token);
    byte[] authenticationData = SerializationUtils.serialize(authentication);
    OAuth2RefreshToken refreshToken = token.getRefreshToken();
    String refreshTokenId = null;
    if (refreshToken != null) {
      refreshTokenId = extractTokenKey(refreshToken.getValue());
    }

    Builder<String, AttributeValue> builder = ImmutableMap.<String, AttributeValue>builder()
        .put("id", new AttributeValue().withS(tokenId))
        .put("type", new AttributeValue().withS(OAUTH2_TYPE))
        .put("authenticationId", new AttributeValue().withS(authenticationId))
        .put("username", new AttributeValue().withS(username))
        .put("clientId", new AttributeValue().withS(clientId))
        .put("token", new AttributeValue().withB(ByteBuffer.wrap(tokenData)))
        .put("authentication", new AttributeValue().withB(ByteBuffer.wrap(authenticationData)));
    if (refreshTokenId != null) {
      builder.put("refreshTokenId", new AttributeValue().withS(refreshTokenId));
    }
    PutItemRequest request =
        new PutItemRequest().withTableName(oauthAccessTokenTable).withItem(builder.build());
    db.putItem(request);
    LOG.info(String.format("OAuth2 access token created for user %s and client %s.", username,
        clientId));

    storeRefreshToken(token, refreshToken, authentication);
  }

  @Override
  public OAuth2AccessToken readAccessToken(String token) {
    GetItemRequest request =
        new GetItemRequest().withTableName(DEFAULT_OAUTH_ACCESS_TOKEN_TABLE)
                            .withKey(DynamoUtils.primaryKey(extractTokenKey(token), OAUTH2_TYPE))
                            .withAttributesToGet("token")
                            .withConsistentRead(true);
    GetItemResult item = db.getItem(request);
    if (item.getItem() == null) {
      return null;
    }

    ByteBuffer buffer = item.getItem().get("token").getB();
    return SerializationUtils.deserialize(buffer.array());
  }

  @Override
  public void removeAccessToken(OAuth2AccessToken token) {
    removeAccessToken(token.getValue());
  }

  public void removeAccessToken(String token) {
    String tokenId = extractTokenKey(token);
    DeleteItemRequest deleteItemRequest =
        new DeleteItemRequest().withTableName(DEFAULT_OAUTH_ACCESS_TOKEN_TABLE)
                               .withKey(DynamoUtils.primaryKey(tokenId, OAUTH2_TYPE))
                               .withReturnValues(ReturnValue.ALL_OLD);
    DeleteItemResult result = db.deleteItem(deleteItemRequest);
    if (result.getAttributes() != null && tokenId.equals(result.getAttributes().get("id").getS())) {
      LOG.info(String.format("Removed access token %s.", tokenId));
    } else {
      LOG.error(String.format("Unable to remove access token %s. It does not exist.", tokenId));
    }
  }

  @Override
  public void storeRefreshToken(OAuth2RefreshToken refreshToken,
      OAuth2Authentication authentication) {
    storeRefreshToken(null, refreshToken, authentication);
  }

  private void storeRefreshToken(OAuth2AccessToken token, OAuth2RefreshToken refreshToken,
      OAuth2Authentication authentication) {
    String tokenId = extractTokenKey(refreshToken.getValue());
    String authenticationId = authenticationKeyGenerator.extractKey(authentication);
    String username = authentication.isClientOnly() ? null : authentication.getName();
    String clientId = authentication.getAuthorizationRequest().getClientId();
    byte[] tokenData = SerializationUtils.serialize(refreshToken);
    byte[] authenticationData = SerializationUtils.serialize(authentication);
    String accessTokenId = null;
    if (token != null) {
      accessTokenId = extractTokenKey(token.getValue());
    }

    Builder<String, AttributeValueUpdate> builder = ImmutableMap.<String, AttributeValueUpdate>builder()
        .put("authenticationId", new AttributeValueUpdate().withValue(new AttributeValue().withS(authenticationId)))
        .put("username", new AttributeValueUpdate().withValue(new AttributeValue().withS(username)))
        .put("clientId", new AttributeValueUpdate().withValue(new AttributeValue().withS(clientId)))
        .put("token", new AttributeValueUpdate().withValue(new AttributeValue().withB(ByteBuffer.wrap(tokenData))))
        .put("authentication", new AttributeValueUpdate().withValue(new AttributeValue().withB(ByteBuffer.wrap(authenticationData))));
    if (accessTokenId != null) {
      builder = builder.put("accessTokenId", new AttributeValueUpdate().withValue(new AttributeValue().withS(accessTokenId)));
    }
    UpdateItemRequest request =
        new UpdateItemRequest().withTableName(oauthAccessTokenTable)
                               .withKey(DynamoUtils.primaryKey(tokenId, REFRESH_TYPE))
                               .withAttributeUpdates(builder.build());
    db.updateItem(request);
    LOG.info(String.format("OAuth2 refresh token updated for user %s and client %s.", username,
        clientId));
  }

  @Override
  public OAuth2RefreshToken readRefreshToken(String token) {
    GetItemRequest request =
        new GetItemRequest().withTableName(DEFAULT_OAUTH_ACCESS_TOKEN_TABLE)
                            .withKey(DynamoUtils.primaryKey(extractTokenKey(token), REFRESH_TYPE))
                            .withAttributesToGet("token")
                            .withConsistentRead(true);
    GetItemResult item = db.getItem(request);
    if (item.getItem() == null) {
      return null;
    }

    ByteBuffer buffer = item.getItem().get("token").getB();
    return SerializationUtils.deserialize(buffer.array());
  }

  @Override
  public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
    return readAuthenticationForRefreshToken(token.getValue());
  }

  public OAuth2Authentication readAuthenticationForRefreshToken(String token) {
    GetItemRequest request =
        new GetItemRequest().withTableName(DEFAULT_OAUTH_ACCESS_TOKEN_TABLE)
                            .withKey(DynamoUtils.primaryKey(extractTokenKey(token), REFRESH_TYPE))
                            .withAttributesToGet("authentication")
                            .withConsistentRead(true);
    GetItemResult item = db.getItem(request);
    if (item.getItem() == null) {
      return null;
    }

    ByteBuffer buffer = item.getItem().get("authentication").getB();
    return SerializationUtils.deserialize(buffer.array());
  }

  @Override
  public void removeRefreshToken(OAuth2RefreshToken token) {
    removeRefreshToken(token.getValue());
  }

  public void removeRefreshToken(String token) {
    String tokenId = extractTokenKey(token);
    DeleteItemRequest deleteItemRequest =
        new DeleteItemRequest().withTableName(DEFAULT_OAUTH_ACCESS_TOKEN_TABLE)
                               .withKey(DynamoUtils.primaryKey(tokenId, REFRESH_TYPE))
                               .withReturnValues(ReturnValue.ALL_OLD);
    DeleteItemResult result = db.deleteItem(deleteItemRequest);
    if (result.getAttributes() != null && tokenId.equals(result.getAttributes().get("id").getS())) {
      LOG.info(String.format("Removed refresh token %s.", tokenId));
    } else {
      LOG.error(String.format("Unable to remove refresh token %s. It does not exist.", tokenId));
    }
  }

  @Override
  public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken token) {
    removeAccessTokenUsingRefreshToken(token.getValue());
  }

  public void removeAccessTokenUsingRefreshToken(String token) {
    String tokenId = extractTokenKey(token);
    GetItemRequest request =
        new GetItemRequest().withTableName(DEFAULT_OAUTH_ACCESS_TOKEN_TABLE)
                            .withKey(DynamoUtils.primaryKey(tokenId, REFRESH_TYPE))
                            .withAttributesToGet("accessTokenId")
                            .withConsistentRead(true);
    GetItemResult item = db.getItem(request);
    if (item.getItem() != null) {
      String accessTokenId = item.getItem().get("accessTokenId").getS();
      DeleteItemRequest deleteItemRequest =
          new DeleteItemRequest().withTableName(DEFAULT_OAUTH_ACCESS_TOKEN_TABLE)
                                 .withKey(DynamoUtils.primaryKey(accessTokenId, OAUTH2_TYPE))
                                 .withReturnValues(ReturnValue.ALL_OLD);
      DeleteItemResult result = db.deleteItem(deleteItemRequest);
      if (result.getAttributes() != null
          && accessTokenId.equals(result.getAttributes().get("id").getS())) {
        LOG.info(String.format("Removed access token %s with a refresh token.", accessTokenId));
      } else {
        LOG.error(String.format(
            "Unable to remove access token %s with a refresh token. It does not exist.",
            accessTokenId));
      }
    }
  }

  @Override
  public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
    String key = authenticationKeyGenerator.extractKey(authentication);
    GetItemRequest request =
        new GetItemRequest().withTableName(DEFAULT_OAUTH_ACCESS_TOKEN_TABLE)
                            .withKey(DynamoUtils.primaryKey(key, OAUTH2_TYPE))
                            .withAttributesToGet("token")
                            .withConsistentRead(true);
    GetItemResult item = db.getItem(request);
    if (item.getItem() == null) {
      return null;
    }

    ByteBuffer buffer = item.getItem().get("token").getB();
    OAuth2AccessToken accessToken = SerializationUtils.deserialize(buffer.array());

    if (accessToken != null
        && !key.equals(authenticationKeyGenerator.extractKey(readAuthentication(accessToken.getValue())))) {
      removeAccessToken(accessToken.getValue());
      // Keep the store consistent (maybe the same user is represented by this authentication but the details have
      // changed)
      storeAccessToken(accessToken, authentication);
    }

    return accessToken;
  }

  @Override
  public Collection<OAuth2AccessToken> findTokensByUserName(String username) {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
    throw new RuntimeException("Not implemented.");
  }

  protected String extractTokenKey(String value) {
    if (value == null) {
      return null;
    }
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
    }

    try {
      byte[] bytes = digest.digest(value.getBytes("UTF-8"));
      return String.format("%032x", new BigInteger(1, bytes));
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
    }
  }
}
