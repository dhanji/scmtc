package com.rethrick.schematic;

import jscheme.JScheme;
import jscheme.SchemeProcedure;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class NettyResponse extends DefaultHttpResponse implements ChannelFutureListener {
  private SchemeProcedure callback;
  private JScheme scheme;

  /**
   * Creates a new instance.
   *
   * @param version the HTTP version of this response
   * @param status  the status of this response
   */
  public NettyResponse(HttpVersion version, HttpResponseStatus status, JScheme scheme) {
    super(version, status);
    this.scheme = scheme;
  }

  public void setBody(String body) {
    setContent(ChannelBuffers.copiedBuffer(body, CharsetUtil.UTF_8));
    setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
  }

  public void setStatus(int status, String reason) {
    setStatus(new HttpResponseStatus(status, reason));
    if (getContent() == null) {
      setBody(reason);
    }
  }

  public void setStatus(int status) {
    setStatus(new HttpResponseStatus(status, Integer.toString(status)));
  }

  public void setCallback(SchemeProcedure procedure) {
    this.callback = procedure;
  }

  @Override
  public void operationComplete(ChannelFuture future) throws Exception {
    // Call back if necessary & close channel.
    try {
      if (callback != null)
        scheme.call(callback);
    } finally {
      ChannelFutureListener.CLOSE.operationComplete(future);
    }
  }

  private static class Callback {
  }
}