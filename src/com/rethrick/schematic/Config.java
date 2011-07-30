package com.rethrick.schematic;

import com.google.sitebricks.options.Options;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Options
public abstract class Config {

  // The app directory
  public String app() {
    return ".";
  }

  public int port() {
    return 8080;
  }
}
