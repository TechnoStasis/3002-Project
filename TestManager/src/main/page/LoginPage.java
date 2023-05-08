package main.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import com.sun.net.httpserver.HttpExchange;

import main.HtmlRenderer;
import main.UserManager;

public class LoginPage extends AbstractPageHandler {

  String htmlPage;
  String htmlErrorPage;

  public LoginPage() {
    htmlPage = HtmlRenderer.readHTML("login.html");
    htmlErrorPage = HtmlRenderer.readHTML("loginerror.html");
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
      cookies.add("user=" + username + ":" + password);
      t.getResponseHeaders().put("Set-Cookie", cookies);
      ArrayList<String> redirect = new ArrayList<String>();
      redirect.add("profile");
      t.getResponseHeaders().put("Location", redirect);
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
