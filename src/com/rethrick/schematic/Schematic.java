package com.rethrick.schematic;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.sitebricks.options.OptionsModule;
import org.apache.commons.io.IOUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Schematic {
  private final HttpServerPipelineFactory pipelineFactory;
  private final Config config;

  @Inject
  public Schematic(HttpServerPipelineFactory pipelineFactory, Config config) {
    this.pipelineFactory = pipelineFactory;
    this.config = config;
  }

  public void start() throws IOException {
    String logo = IOUtils.toString(Schematic.class.getResourceAsStream("logo"), "utf-8")
        .replace("@{version}", "1.0");
    System.out.println(logo);

    // Configure the server.
    ServerBootstrap bootstrap = new ServerBootstrap(
        new NioServerSocketChannelFactory(
            Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool()));

    // Set up the event pipeline factory.
    bootstrap.setPipelineFactory(pipelineFactory);

    // Bind and start to accept incoming connections.
    bootstrap.bind(new InetSocketAddress(config.port()));
    System.out.println("scmtc is running on port " + config.port());
    System.out.println();
  }

  public static void main(String[] args) throws IOException {
    Guice.createInjector(new OptionsModule(args).options(Config.class))
        .getInstance(Schematic.class)
        .start();
  }
}
