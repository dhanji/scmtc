package com.rethrick.schematic;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class TabSyntaxRewriter {
  public static String rewrite(Reader scheme) throws IOException {
    String string = IOUtils.toString(scheme);
    if (!string.trim().startsWith("{tab-syntax}")) {
      return string;
    }

    @SuppressWarnings("unchecked")
    List<String> lines = IOUtils.readLines(new StringReader(string));
    lines.remove(0);

    StringBuilder builder = new StringBuilder();

    boolean inQuote = false;
    int rParens = 0;
    for (int i = 0, linesSize = lines.size(); i < linesSize; i++) {
      String line = lines.get(i).trim();

      if (line.isEmpty()) {
        builder.append("\n");
        continue;
      }

      boolean extraRParen = false;
      // Process line.
      String[] split = line.split("[ ]");
      StringBuilder lb = new StringBuilder();
      for (String part : split) {
        int quoteAt = part.indexOf("\"");
        if (quoteAt > -1) {
          inQuote = !inQuote;
        }

        if (!inQuote) {
          if ("::".equals(part)) {
            part = "(";
            extraRParen = true;
          } else
            part = part.replace("[", "(").replace("]", ")");
        }
        lb.append(part);
        lb.append(' ');
      }

      if (inQuote) {
        builder.append(lb.toString());
        continue;
      }

      builder.append('(');
      rParens++;
      builder.append(lb.toString());
      if (extraRParen)
        builder.append(")");

      // Only close if this is the last line or there's a gap.
      if (i == linesSize - 1 || lines.get(i + 1).trim().isEmpty()) {
        for (int j = 0; j < rParens; j++) {
          builder.append(")");
        }
        rParens = 0;
      }
      builder.append('\n');
    }

    return builder.toString();
  }
}
