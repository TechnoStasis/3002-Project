package main.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

import main.HtmlRenderer;
import main.QuizManager;
import main.UserManager;

public class ProfilePage extends AbstractPageHandler {

  private final String htmlPage;

  public ProfilePage() {
    htmlPage = HtmlRenderer.readHTML("profile.html");
  }

  @Override
  public void handleGet(HttpExchange t) throws IOException {
    String user = "";
    String password = "";
    ArrayList<String> redirect = new ArrayList<>();
    if (t.getRequestHeaders().get("Cookie") != null) {
      for (String str : t.getRequestHeaders().get("Cookie")) {
        user = str.split("=")[1].split(":")[0];
        password = str.split("=")[1].split(":")[1];
      }
    }

    if (!UserManager.INSTANCE.validate(user, password)) {
      redirect.add("logout");
      t.getResponseHeaders().put("Location", redirect);
      t.sendResponseHeaders(302, -1);
      t.close();
    }

    if (user.isEmpty()) {
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
    String user = "";
    if (t.getRequestHeaders().get("Cookie") != null) {
      for (String str : t.getRequestHeaders().get("Cookie")) {
        user = str.split("=")[1].split(":")[0];
        }
    }
    QuizManager.INSTANCE.createNewQuiz(UserManager.INSTANCE.getUser(user));
  }

}
