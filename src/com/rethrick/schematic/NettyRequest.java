package com.rethrick.schematic;

import com.google.common.collect.Maps;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.util.Map;

/**
 * Request wrapper for Netty HTTP requests.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class NettyRequest {
  private final Map<String, Object> props = Maps.newHashMap();

  public NettyRequest(HttpRequest request, Channel channel) {
    // Convert to local map.
    props.put("uri", request.getUri());
    props.put("method", request.getMethod().toString().toLowerCase());
    props.put("content-length", request.getHeader("Content-Length"));
    props.put("content-type", request.getHeader("Content-Type"));
    props.put("user-agent", request.getHeader("User-Agent"));

    props.put("ip", channel.getRemoteAddress().toString());
//    if (!request.getUri().isEmpty())
//      props.put("scheme", request.getUri().substring(0, request.getUri().indexOf(":")));
  }
}