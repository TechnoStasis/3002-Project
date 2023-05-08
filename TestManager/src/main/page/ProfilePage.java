package main.page;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

import main.HtmlRenderer;
import main.TestManager;
import main.UserManager;

public class ProfilePage extends AbstractPageHandler {

  private final String htmlPage;

  public ProfilePage() {
    StringBuilder contentBuilder = new StringBuilder();
    try {
      BufferedReader in = new BufferedReader(
          new FileReader(TestManager.TEMPLATE_PATH + "profile.html"));
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
  public void handleGet(HttpExchange t) throws IOException {
    String user = "";
    String password = "";
    if (t.getRequestHeaders().get("Cookie") != null) {
      for (String str : t.getRequestHeaders().get("Cookie")) {
        user = str.split("=")[1].split(":")[0];
        password = str.split("=")[1].split(":")[1];
      }
    }

    if (!UserManager.INSTANCE.validate(user, password)) {
      ArrayList<String> redirect = new ArrayList<>();
      redirect.add("logout");
      t.getResponseHeaders().put("Location", redirect);
      t.sendResponseHeaders(302, -1);
      t.close();
    }

    if (user.isEmpty()) {
      ArrayList<String> redirect = new ArrayList<>();
      redirect.add("login");
      t.getResponseHeaders().put("Location", redirect);
      t.sendResponseHeaders(302, -1);
      t.close();
    }

    HashMap<String, Object> dataToHTML = new HashMap<String, Object>();
    dataToHTML.put("username", user);

    String htmlPage = HtmlRenderer.render(this.htmlPage, dataToHTML);
    t.sendResponseHeaders(200, htmlPage.length());
    t.getResponseBody().write(htmlPage.getBytes());
    t.getResponseBody().close();
  }

  @Override
  public void handlePost(HttpExchange t) throws IOException {

    throw new UnsupportedOperationException("Unimplemented method 'handlePost'");
  }

}
