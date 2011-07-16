package com.rethrick.schematic;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class TabSyntaxRewriterTest {
  @Test
  public final void rewrite() throws IOException {
    String rewrite =
        TabSyntaxRewriter.rewrite(new StringReader(
            " define get (lambda (x) (x + 1))\n\n" +
            " [1 2 3 4] \" []asdaosd [ ] [ ] \"\n" +
                "case ls \n" +
                "  []  :: reverse! string"
        ));

    System.out.println(rewrite);
  }
}
