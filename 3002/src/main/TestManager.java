package main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpServer;

import main.context.HomeHandler;

public class TestManager {

  private static void log(Object s) {
    System.out.println(s);
  }

  private static void registerContexts(HttpServer s) {
    s.createContext("/home", new HomeHandler());
  }

  public static void main(String[] args) {
    try {
      HttpServer s = HttpServer.create(new InetSocketAddress(8081), 0);

      ThreadPoolExecutor exec = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

      registerContexts(s);
      s.setExecutor(exec);
      s.start();

      
      log("Starting server");
    } catch (IOException e) {
      log("Failed to start server");
      e.printStackTrace();
    }
  }

}
