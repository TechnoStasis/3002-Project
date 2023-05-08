package main.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

import main.HtmlRenderer;
import main.UserManager;

public class LoginPage extends AbstractPageHandler {

  String htmlPage;
  public LoginPage() {
    htmlPage = HtmlRenderer.readHTML("login.html");
  }
  
  @Override
  public void handleGet(HttpExchange t) throws IOException {
    HashMap<String, Object> hiddenError = new HashMap<>();
    hiddenError.put("error", "");
    String htmlPage = HtmlRenderer.render(this.htmlPage, hiddenError);
    t.sendResponseHeaders(200, htmlPage.length());
    OutputStream os = t.getResponseBody();
    os.write(htmlPage.getBytes());
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

    if (UserManager.INSTANCE.validate(username, password)) {
      ArrayList<String> cookies = new ArrayList<String>();
      cookies.add("user=" + username + ":" + password);
      t.getResponseHeaders().put("Set-Cookie", cookies);
      ArrayList<String> redirect = new ArrayList<String>();
      redirect.add("profile");
      t.getResponseHeaders().put("Location", redirect);
      t.sendResponseHeaders(302, -1);
      t.close();
    } else {
      HashMap<String, Object> hiddenError = new HashMap<>();
      hiddenError.put("error", HtmlRenderer.appendError("Wrong username or password!"));
      String htmlPage = HtmlRenderer.render(this.htmlPage, hiddenError);
      t.sendResponseHeaders(200, htmlPage.length());
      OutputStream os = t.getResponseBody();
      os.write(htmlPage.getBytes());
      os.close();
    }
  }
}
