package com.srctran.backend.tools;

import java.util.Date;

import com.amazonaws.util.DateUtils;

public class GetCurrentDate {

  public static void main(String[] args) {
    System.out.println(new DateUtils().formatIso8601Date(new Date()));
  }
}
