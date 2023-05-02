package main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpServer;

import main.context.HomeHandler;

public class TestManager {

  private static void registerContext(HttpServer s) {
    s.createContext("/", new HomeHandler());
  }

  public static void main(String[] args) {
		try {
      InetSocketAddress a = new InetSocketAddress("localhost", 8081);

			HttpServer server = HttpServer.create(a, 0);
			ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
			
			registerContext(server);
			server.setExecutor(threadPoolExecutor);
			server.start();
			System.out.println("Starting server at " + a.getHostName() + ":" + a.getPort());

		} catch (IOException e) {
			System.out.println("Failed to initialize server");
			e.printStackTrace();
		}
	}
}