package main.context;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class LoginHandler implements HttpHandler {

  String htmlPage;

  public LoginHandler() {
    StringBuilder contentBuilder = new StringBuilder();
    try {
      BufferedReader in = new BufferedReader(
          new FileReader("/Users/yvesreyes/Documents/3002-Project/TestManager/src/main/context/login.html"));
      String str;
      while ((str = in.readLine()) != null) {
        contentBuilder.append(str);
      }
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    htmlPage = contentBuilder.toString();
  }

  @Override
  public void handle(HttpExchange t) throws IOException {
    String request = t.getRequestMethod();
    if (request.equals("GET")) {
      String response = htmlPage;
      t.sendResponseHeaders(200, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }

    if (request.equals("POST")) {
      InputStream io = t.getRequestBody();
      InputStreamReader inputStreamReader = new InputStreamReader(io);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      String req = bufferedReader.readLine();
      String[] details = req.split("&");

      System.out.println("username: " + details[0].split("=")[1]);
      System.out.println("password: " + details[1].split("=")[1]);
    }
  }
}
