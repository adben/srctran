package com.srctran.backend.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserPasswordEncoder {

  public static void main(String[] args) throws IOException {
    AbstractApplicationContext context =
        new ClassPathXmlApplicationContext("application-context.xml");
    context.registerShutdownHook();

    try {
      PasswordEncoder passwordEncoder =
          context.getBean("userPasswordEncoder", PasswordEncoder.class);

      System.out.print("Password to encode: ");
      String password = new BufferedReader(new InputStreamReader(System.in)).readLine();
      System.out.print("Encoded password:   ");
      System.out.println(passwordEncoder.encode(password));
    }
    finally {
      context.close();
    }
  }
}
