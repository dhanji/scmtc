package com.rethrick.schematic;

import com.google.sitebricks.options.Options;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Options
public abstract class Config {
  public int port() {
    return 8080;
  }
}
