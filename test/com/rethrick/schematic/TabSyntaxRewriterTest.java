package com.rethrick.schematic;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class TabSyntaxRewriterTest {
  @Test
  public final void rewrite() throws IOException {
    String rewrite =
        TabSyntaxRewriter.rewrite(read("example.scmtc"));

    assertEquals("(define get  (lambda (request response)  (begin  (display #{handling request.." +
        ".}# ) (display request/uri request ) (body response #{OK}# ) (body status 200  ))))", rewrite);
  }

  @Test
  public final void rewriteWithHeredocs() throws IOException {
    String rewrite =
        TabSyntaxRewriter.rewrite(read("example_heredoc.scmtc"));

    assertEquals("(define get  (lambda (request response)  (begin  (display #{handling request..." +
        "   #[request-uri request]#}# ) (display request/uri request ) (body response #{OK}# ) " +
        "(body status 200  ))))", rewrite);
  }

  @Test
  public final void rewriteWithPatterLines() throws IOException {
    String rewrite =
        TabSyntaxRewriter.rewrite(read("example_pline.scmtc"), true);

    System.out.println(rewrite);
  }

  private static InputStreamReader read(String name) {
    return new InputStreamReader(TabSyntaxRewriterTest.class.getResourceAsStream(name));
  }
}
