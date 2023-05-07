package main.page;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import main.TestManager;

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
    if (t.getRequestHeaders().get("Cookie") != null) {
      for (String str : t.getRequestHeaders().get("Cookie")) {
        user = str.split("=")[1];
        user = user.split(":")[0];
      }
    }

    String htmlPage = this.htmlPage.replace("{{username}}", user);
    t.sendResponseHeaders(200, htmlPage.length());
    t.getResponseBody().write(htmlPage.getBytes());
    t.getResponseBody().close();
  }

  @Override
  public void handlePost(HttpExchange t) throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handlePost'");
  }

}
