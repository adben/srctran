package com.srctran.backend.server;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.srctran.backend.project.ProjectResource;

public class Main {

  public static final URI BASE_URI = URI.create("http://localhost:8080/");

  public static void main(String[] args) throws IOException {
    ResourceConfig resourceConfig = new ResourceConfig(
        ProjectResource.class,
        JsonObjectMapperProvider.class,
        JacksonFeature.class);
    HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig);

    System.out.println(String.format("Application started.\nTry out %s\nHit enter to stop it...",
            BASE_URI));
    System.in.read();

    server.stop();
  }
}
