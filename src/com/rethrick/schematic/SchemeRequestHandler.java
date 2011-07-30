package com.rethrick.schematic;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import jscheme.JScheme;
import org.apache.commons.io.IOUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class SchemeRequestHandler extends SimpleChannelUpstreamHandler {
  private static final Logger log = LoggerFactory.getLogger(SchemeRequestHandler.class);
  private final Config config;
  private final String pathPrefix;

  @Inject
  public SchemeRequestHandler(Config config) {
    this.config = config;
    this.pathPrefix = config.app().endsWith("/") ? config.app() : config.app() + "/";
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    HttpRequest request = (HttpRequest) e.getMessage();
    String uri = request.getUri();

    // Prefer .scmtc files to regular .scm ones if they exist.
    String name = uri.equals("/") || uri.equals("") ? "home" : uri;
    String filename = pathPrefix + name + ".scmtc";
    String script;

    if (new File(filename).exists())
      script = TabSyntaxRewriter.rewrite(new FileReader(filename), true);
    else if (new File(filename = (pathPrefix + name + ".scm")).exists())
      script = IOUtils.toString(new FileReader(filename));
    else {
      serveStaticFile(ctx, e, request, uri);
      return;
    }

    JScheme scheme = new JScheme();
    NettyResponse response = new NettyResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, scheme);
    try {
      // Reload our scripts every time in development mode.
      scheme.load(
          new InputStreamReader(SchemeRequestHandler.class.getResourceAsStream("schematic.scm")));
      scheme.eval("(define --file-- \"" + config.app() + "/" + name + "\")");
      scheme.eval(script);


      scheme.call(request.getMethod().getName().toLowerCase(), request, response);
    } catch (Exception ex) {
      response.setBody(ex.getMessage()
          + "<br>"
          + script
      );
    }

    // Render response!
    Channel channel = e.getChannel();
    ChannelFuture future = channel.write(response);

    // Close the out channel when it is done writing.
    future.addListener(response);
    future.addListener(ChannelFutureListener.CLOSE);
  }

  private void serveStaticFile(ChannelHandlerContext ctx, MessageEvent e, HttpRequest request, String uri) throws IOException {
    if (request.getMethod() != HttpMethod.GET) {
      sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
      return;
    }

    if (uri.startsWith("/"))
      uri = uri.substring(1);
    uri = System.getProperty("user.dir") + File.separator + pathPrefix + uri.replace("/",
        File.separator);
    // There is no script to handle this, so serve as static file instead.
    File file = new File(uri);

    if (!file.exists() || file.isHidden()) {
      sendError(ctx, HttpResponseStatus.NOT_FOUND);
      return;
    }

    if (!file.isFile()) {
      sendError(ctx, FORBIDDEN);
      return;
    }

    log.trace("Serving: " + uri);

    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
    setContentLength(response, file.length());
    response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
    System.out.println("File length was " + file.length());

    Channel ch = e.getChannel();

    // Write the initial line and the header.
    ch.write(response);

    // Write the content.
    ChannelFuture writeFuture;
    // Cannot use zero-copy with HTTPS.
    System.out.println("using chunked xfer");
    writeFuture = ch.write(new ChunkedFile(file));
    writeFuture.addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        System.out.println("File Service completed.");
      }
    });

    // Close the connection when the whole content is written out.
    writeFuture.addListener(ChannelFutureListener.CLOSE);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
      throws Exception {
    ErrorResponse response = new ErrorResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    response.setContent(ChannelBuffers.copiedBuffer("Scheme Environment Error " + e.getCause()
        .getMessage(), Charset.forName("utf-8")));
    ctx.getChannel()
        .write(response)
        .addListener(ChannelFutureListener.CLOSE);
  }

  private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
    HttpResponse response = new ErrorResponse(status);
    response.setContent(ChannelBuffers.copiedBuffer(
        "Error: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

    // Close the connection as soon as the error message is sent.
    ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
  }

  public static class ErrorResponse extends DefaultHttpResponse {

    /**
     * Creates a new instance.
     *
     * @param status the status of this response
     */
    public ErrorResponse(HttpResponseStatus status) {
      super(HttpVersion.HTTP_1_1, status);
      setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
    }
  }
}
