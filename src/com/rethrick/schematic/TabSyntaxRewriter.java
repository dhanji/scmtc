package com.rethrick.schematic;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class TabSyntaxRewriter {
  public static String rewrite(Reader scheme) throws IOException {
      return rewrite(scheme, false);
  }

  public static String rewrite(Reader scheme, boolean multiline) throws IOException {
    @SuppressWarnings("unchecked")
    List<String> lines = IOUtils.readLines(scheme);
    StringBuilder builder = new StringBuilder();

    boolean inQuote = false;
    int indentLevel = 0;
    for (int i = 0, linesSize = lines.size(); i < linesSize; i++) {
      String rawLine = lines.get(i);
      if (rawLine.isEmpty()) {
        if (inQuote)
          builder.append(rawLine).append('\n');
        continue;
      }

      indentLevel = detectIndentation(rawLine);
      String line = rawLine.trim();

      // Skip comments.
      if (!inQuote && line.startsWith(";"))
        continue;

      boolean extraRParen = false;
      // Process line.
      String[] split = line.split("[ ]");
      StringBuilder lb = new StringBuilder();
      int lerps = 0;
      for (String part : split) {
        String trim = part.trim();

        // Close up any lerps. (has to happen up here as we may close the string partway thru.
        boolean lerpEnded = false;
        if (inQuote) {
          int endLerp = part.indexOf("}");
          if (lerps > 0 && endLerp > -1) {
            part = part.substring(0, endLerp) + "]#" + part.substring(endLerp + 1);
            lerps--;
            lerpEnded = true;
          }
        }

        int quoteAt = part.indexOf("\"");
        if (quoteAt > -1) {
          // If there is another quote inside this same part, skip it.
          int endQuoteAt = part.indexOf("\"", quoteAt + 1);
          if (endQuoteAt == -1) {
            inQuote = !inQuote;
          }

          if (endQuoteAt > -1) {
            // replace " with #{ (quasi-string syntax)
            part = part.substring(0, quoteAt) + "#{" + part.substring(quoteAt + 1);
            endQuoteAt++; // account for larger quasi-string delimiter '#{'
            part = part.substring(0, endQuoteAt) + "}#" + part.substring(endQuoteAt + 1);
          } else {
            // replace " with #{ (quasi-string syntax)
            part = part.substring(0, quoteAt) + (inQuote ? "#{" : "}#") + part.substring(quoteAt + 1);
          }
        }

        if (!inQuote && !lerpEnded) {
          if ("::".equals(part)) {
            part = "(";
            extraRParen = true;
          } else if ("=>".equals(part)) {
            part = "";  //elide hashrockets
          } else if ("[]".equals(part)) {
            part = "()";
          } else
            part = part.replace("[", "(list ").replace("]", ")");
        } else {
          // Interpolate strings.
          int startLerp = part.indexOf("@{");
          if (startLerp > -1) {
            part = part.replace("@{", "#[");
            int endLerp = part.indexOf("}", startLerp);
            lerps++;
            if (endLerp > -1) {
              part = part.substring(0, endLerp) + "]#" + part.substring(endLerp + 1);
              lerps--;
            }
          }
        }
        lb.append(part);
        lb.append(' ');
      }

      if (inQuote) {
        builder.append(lb.toString());
        continue;
      }

      builder.append('(');
      builder.append(lb.toString());
      if (extraRParen)
        builder.append(")");

      // Only close if this is the last line or there's a drop in the indent level.
      int nextLineIndentLevel = i < linesSize - 1 ? detectIndentation(lines.get(i + 1)) : 0;
      if (nextLineIndentLevel == indentLevel)
        builder.append(')');

      if (indentLevel > nextLineIndentLevel) {
        int rParenCount = (nextLineIndentLevel - indentLevel) / 2;

        for (int j = 0; j < rParenCount; j++) {
          builder.append(')');
        }
      }

      builder.append(multiline ? '\n' : ' ');
    }

    // Close up rparens.
    int rparens = (indentLevel / 2) + 1;
    for (int j = 0; j < rparens; j++) {
      builder.append(')');
    }

    return builder.toString();
  }

  private static int detectIndentation(String line) {
    char[] chars = line.toCharArray();
    int i;
    for (i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (!Character.isWhitespace(c)) {
        break;
      }
    }
    return i;
  }
}
