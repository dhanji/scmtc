package com.rethrick.schematic;

import org.mvel2.templates.TemplateRuntime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Tools {
  public static String template(String file) throws FileNotFoundException {
    return TemplateRuntime.eval(new FileInputStream(new File(file))).toString();
  }
}
