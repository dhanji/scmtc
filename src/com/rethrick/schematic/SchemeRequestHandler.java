package com.rethrick.schematic;

import com.google.inject.Singleton;
import jscheme.JScheme;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class SchemeRequestHandler extends SimpleChannelHandler {

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    HttpRequest request = (HttpRequest) e.getMessage();
    JScheme scheme = new JScheme();
    NettyResponse response = new NettyResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, scheme);

    // TODO(dhanji): Make a route config file later.
    // Reload our scripts every time in development mode.
    scheme.load(new InputStreamReader(SchemeRequestHandler.class.getResourceAsStream("schematic.scm")));
    scheme.load(TabSyntaxRewriter.rewrite(new FileReader("home.scm")));
    scheme.call(request.getMethod().getName().toLowerCase(), request, response);

    // Render response!
    Channel channel = e.getChannel();
    ChannelFuture future = channel.write(response);

    // Close the out channel when it is done writing.
    future.addListener(response);
  }

  @Override public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
      throws Exception {
    ErrorResponse response = new ErrorResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    response.setContent(ChannelBuffers.copiedBuffer("Scheme Environment Error " + e.getCause()
        .getMessage(), Charset.forName("utf-8")));
    ctx.getChannel()
        .write(response)
        .addListener(ChannelFutureListener.CLOSE);
  }

  public static class ErrorResponse extends DefaultHttpResponse {

    /**
     * Creates a new instance.
     *
     * @param status  the status of this response
     */
    public ErrorResponse(HttpResponseStatus status) {
      super(HttpVersion.HTTP_1_1, status);
    }
  }
}
