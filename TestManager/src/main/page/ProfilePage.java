package main.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

import main.HtmlHelper;
import main.QuizManager;
import main.UserManager;
import main.quiz.Quiz;

public class ProfilePage extends AbstractPageHandler {

  private final String htmlPage;

  public ProfilePage() {
    htmlPage = HtmlHelper.readHTML("profile.html");
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

    if (!UserManager.INSTANCE.validate(user, password))
      redirect(t, "logout");

    if (user.isEmpty())
      redirect(t, "login");

    HashMap<String, Object> dataToHTML = new HashMap<String, Object>();
    dataToHTML.put("username", user);

    String pastQuizzes = "";
    ArrayList<Quiz> past = QuizManager.INSTANCE.getPastQuizzes(UserManager.INSTANCE.getUser(user));
    for (Quiz q : past)
      pastQuizzes = pastQuizzes
          + HtmlHelper
              .paragraphTag(HtmlHelper.boldTag(q.getType().toUpperCase() + " ") + q.getPath().replace("T", " ")
                  + HtmlHelper.boldTag(" Marks: " + q.totalMarks() + "/30"));

    dataToHTML.put("pastquizzes", pastQuizzes);

    if (QuizManager.INSTANCE.getCurrentQuiz(UserManager.INSTANCE.getUser(user)) == null) {
      dataToHTML.put("button1", HtmlHelper.createSubmitButton("C", "Start New C Quiz"));
      dataToHTML.put("button2", HtmlHelper.createSubmitButton("P", "Start New Python Quiz"));
    } else {
      dataToHTML.put("button1", HtmlHelper.createSubmitButton("continue", "Continue"));
      dataToHTML.put("button2", "");
    }

    String htmlPage = HtmlHelper.render(this.htmlPage, dataToHTML);

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

    InputStream io = t.getRequestBody();
    InputStreamReader inputStreamReader = new InputStreamReader(io);
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    String answer = bufferedReader.readLine();

    System.out.println(answer);

    if (QuizManager.INSTANCE.getCurrentQuiz(UserManager.INSTANCE.getUser(user)) == null)
      QuizManager.INSTANCE.createNewQuiz(UserManager.INSTANCE.getUser(user), answer.split("=")[0]);




    redirect(t, "quiz");
  }

}
