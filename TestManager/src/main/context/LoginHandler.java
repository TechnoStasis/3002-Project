package main.context;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import com.sun.net.httpserver.HttpExchange;
import main.UserManager;

public class LoginHandler extends AbstractPageHandler {

  String htmlPage;
  String htmlErrorPage;

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

    StringBuilder contentBuilder2 = new StringBuilder();
    try {
      BufferedReader in = new BufferedReader(
          new FileReader("/Users/yvesreyes/Documents/3002-Project/TestManager/src/main/context/loginerror.html"));
      String str;
      while ((str = in.readLine()) != null) {
        contentBuilder2.append(str);
      }
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    htmlErrorPage = contentBuilder2.toString();
  }
  
  @Override
  public void handleGet(HttpExchange t) throws IOException {
    String response = htmlPage;
    t.sendResponseHeaders(200, response.length());
    OutputStream os = t.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  @Override
  public void handlePost(HttpExchange t) throws IOException {
    InputStream io = t.getRequestBody();
    InputStreamReader inputStreamReader = new InputStreamReader(io);
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    String req = bufferedReader.readLine();
    String[] details = req.split("&");

    String username = details[0].split("=")[1];
    String password = details[1].split("=")[1];

    if (UserManager.INSTANCE.getUser(username, password) != null) {
      ArrayList<String> cookies = new ArrayList<String>();
      cookies.add("user=" + username + "|" + password);
      t.getResponseHeaders().put("Set-Cookie", cookies);
      cookies.clear();
      cookies.add("home");
      t.getResponseHeaders().put("Location", cookies);
      t.sendResponseHeaders(302, -1);
      t.close();
    } else {
      String response = htmlErrorPage;
      t.sendResponseHeaders(200, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }
}
