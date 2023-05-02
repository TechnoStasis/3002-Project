package main.context;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HomeHandler implements HttpHandler {

  @Override
  public void handle(HttpExchange t) throws IOException {
    String response = "Hello, World!\n";
    t.sendResponseHeaders(200, response.length());
    OutputStream os = t.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }
}
