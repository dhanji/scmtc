package com.rethrick.schematic;

import org.mvel2.templates.TemplateRuntime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Tools {
  public static String template(String file, Map<String, String> vars) throws FileNotFoundException {
    return TemplateRuntime.eval(new FileInputStream(new File(file)), vars).toString();
  }
}
