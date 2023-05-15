package main.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

import main.HtmlHelper;
import main.QuizManager;
import main.UserManager;
import main.quiz.Quiz;
import users.User;

public class QuizPage extends AbstractPageHandler {

    String htmlPage;

    public QuizPage() {
        htmlPage = HtmlHelper.readHTML("quiz.html");
    }

    @Override
    public void handleGet(HttpExchange t) throws IOException {
        String user = "";
        if (t.getRequestHeaders().get("Cookie") != null) {
            for (String str : t.getRequestHeaders().get("Cookie")) {
                user = str.split("=")[1].split(":")[0];
            }
        }

        if (!t.getRequestURI().toASCIIString().contains("?=")) 
             redirect(t, "quiz?=1");
        

        String currentQuestion = t.getRequestURI().toASCIIString().split("=")[1];

        Quiz q = QuizManager.INSTANCE.getCurrentQuiz(UserManager.INSTANCE.getUser(user));
        int cQ = Integer.parseInt(currentQuestion);
        int attempts = q.getNumberOfAttempts(cQ);
        String id = q.getQuestionId(cQ);

        HashMap<String, Object> data = new HashMap<>();
        data.put("questionnumber", currentQuestion);
        data.put("attempts", attempts + "");
        data.put("questionID", id.toUpperCase());
        data.put("button",
                attempts > 0 ? HtmlHelper.createButton("Submit") : HtmlHelper.appendError("No More Attempts"));

        String htmlPage = HtmlHelper.render(this.htmlPage, data);

        t.sendResponseHeaders(200, htmlPage.length());
        t.getResponseBody().write(htmlPage.getBytes());
        t.getResponseBody().close();
    }

    @Override
    public void handlePost(HttpExchange t) throws IOException {

        String username = "";
        String password = "";
        if (t.getRequestHeaders().get("Cookie") != null) {
            for (String str : t.getRequestHeaders().get("Cookie")) {
                username = str.split("=")[1].split(":")[0];
                password = str.split("=")[1].split(":")[1];
            }
        }

        UserManager.INSTANCE.validate(username, password);
        User user = UserManager.INSTANCE.getUser(username);

        InputStream io = t.getRequestBody();
        InputStreamReader inputStreamReader = new InputStreamReader(io);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String answer = bufferedReader.readLine().replace("+", " ");

        int currentQuestion = Integer.parseInt(t.getRequestURI().toASCIIString().split("=")[1]);
        Quiz q = QuizManager.INSTANCE.getCurrentQuiz(user);

        // QUIZ BANK COMMUNICATION

        int attempts = q.getNumberOfAttempts(currentQuestion);

        q.setNumberOfAttempts(currentQuestion, attempts - 1);
        q.save();

        redirect(t, t.getRequestURI().toString());
    }
}
