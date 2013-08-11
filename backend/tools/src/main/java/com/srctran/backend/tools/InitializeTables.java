package com.srctran.backend.tools;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.srctran.backend.entity.user.User;
import com.srctran.backend.entity.user.User.Status;

public class InitializeTables {

  private static final Logger LOG = Logger.getLogger(InitializeTables.class);

  private static final String OAUTH_TOKENS_TABLE = "oauth_tokens";
  private static final String USERS_TABLE = "users";
  private static final String[] TABLES = new String[] {OAUTH_TOKENS_TABLE, USERS_TABLE};

  private static final PasswordEncoder passwordEncoder;
  private static final AmazonDynamoDBClient db;
  private static final DynamoDBMapper dbMapper;

  static {
    @SuppressWarnings("resource")
    AbstractApplicationContext context =
        new ClassPathXmlApplicationContext("application-context.xml");
    context.registerShutdownHook();

    passwordEncoder = context.getBean("passwordEncoder", PasswordEncoder.class);
    db = context.getBean("db", AmazonDynamoDBClient.class);
    dbMapper = context.getBean("dbMapper", DynamoDBMapper.class);
  }

  private static final User[] USERS = new User[] {
    new User("tfeng", "huining.feng@gmail.com", passwordEncoder.encode("password"), Status.ACTIVE),
    new User("ahuang", "amywmhuang@gmail.com", passwordEncoder.encode("password"), Status.ACTIVE)
  };

  public static void main(String[] args) {
    // Delete tables
    for (String tableName : TABLES) {
      try {
        db.deleteTable(new DeleteTableRequest().withTableName(tableName));
      } catch (ResourceNotFoundException e) {
        // Table already deleted. Ignore.
      }
    }

    waitForTableDeletion();

    createUsersTable();
    createOauthTokensTable();

    waitForTableCreation();

    insertUsers();
  }

  private static void waitForTableDeletion() {
    LOG.info("Waiting for table deletion.");

    boolean wait = true;
    while (wait) {
      wait = false;
      ListTablesResult tables = db.listTables();
      for (String tableName : TABLES) {
        if (tables.getTableNames().contains(tableName)) {
          wait = true;
          break;
        }
      }

      if (wait) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // Ignore.
        }
      }
    }
  }

  private static void waitForTableCreation() {
    LOG.info("Waiting for table creation.");

    boolean wait = true;
    while (wait) {
      wait = false;
      for (String tableName : TABLES) {
        DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
        TableDescription tableDescription = db.describeTable(request).getTable();
        if (!TableStatus.ACTIVE.toString().equals(tableDescription.getTableStatus())) {
          wait = true;
          break;
        }
      }

      if (wait) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // Ignore.
        }
      }
    }
  }

  private static void createUsersTable() {
    CreateTableRequest createTableRequest =
        new CreateTableRequest().withTableName(USERS_TABLE)
        .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1l)
                                                              .withWriteCapacityUnits(1l))
        .withKeySchema(
            new KeySchemaElement().withKeyType(KeyType.HASH).withAttributeName("username"),
            new KeySchemaElement().withKeyType(KeyType.RANGE).withAttributeName("type"))
        .withAttributeDefinitions(
            new AttributeDefinition().withAttributeName("username").withAttributeType(ScalarAttributeType.S),
            new AttributeDefinition().withAttributeName("type").withAttributeType(ScalarAttributeType.S));
    db.createTable(createTableRequest);
  }

  private static void createOauthTokensTable() {
    CreateTableRequest createTableRequest =
        new CreateTableRequest().withTableName(OAUTH_TOKENS_TABLE)
        .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1l)
                                                              .withWriteCapacityUnits(1l))
        .withKeySchema(
            new KeySchemaElement().withKeyType(KeyType.HASH).withAttributeName("id"),
            new KeySchemaElement().withKeyType(KeyType.RANGE).withAttributeName("type"))
        .withAttributeDefinitions(
            new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S),
            new AttributeDefinition().withAttributeName("type").withAttributeType(ScalarAttributeType.S));
    db.createTable(createTableRequest);
  }

  private static void insertUsers() {
    dbMapper.batchSave((Object[]) USERS);
  }
}
