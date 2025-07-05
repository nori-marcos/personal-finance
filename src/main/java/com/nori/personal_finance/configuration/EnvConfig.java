package com.nori.personal_finance.configuration;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig {
  private static final Dotenv dotenv = Dotenv.configure()
                                           .ignoreIfMissing()
                                           .load();

  public static String get(final String key) {
    final String sysValue = System.getenv(key);
    return sysValue != null ? sysValue : dotenv.get(key);
  }

  private EnvConfig() {}
}
