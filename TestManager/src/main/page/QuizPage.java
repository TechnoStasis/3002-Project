package main.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

import main.HtmlRenderer;
import main.QuizManager;
import main.UserManager;
import main.quiz.Quiz;

public class QuizPage extends AbstractPageHandler {

    String htmlPage;

    public QuizPage() {
        htmlPage = HtmlRenderer.readHTML("quiz.html");
    }

    @Override
    public void handleGet(HttpExchange t) throws IOException {
        String user = "";
        if (t.getRequestHeaders().get("Cookie") != null) {
            for (String str : t.getRequestHeaders().get("Cookie")) {
                user = str.split("=")[1].split(":")[0];
            }
        }

        if (!t.getRequestURI().toASCIIString().contains("?=")) {
            ArrayList<String> redir = new ArrayList<>();
            redir.add("quiz?=1");
            t.getResponseHeaders().put("Location", redir);
            t.sendResponseHeaders(302, -1);
            t.close();
        }

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
                attempts > 0 ? HtmlRenderer.createButton("Submit") : HtmlRenderer.appendError("No More Attempts"));

        String htmlPage = HtmlRenderer.render(this.htmlPage, data);

        t.sendResponseHeaders(200, htmlPage.length());
        t.getResponseBody().write(htmlPage.getBytes());
        t.getResponseBody().close();
    }

    @Override
    public void handlePost(HttpExchange t) throws IOException {

        InputStream io = t.getRequestBody();
        InputStreamReader inputStreamReader = new InputStreamReader(io);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String answer = bufferedReader.readLine().replace("+", " ");

        String user = "";
        if (t.getRequestHeaders().get("Cookie") != null) {
            for (String str : t.getRequestHeaders().get("Cookie")) {
                user = str.split("=")[1].split(":")[0];
            }
        }

        if (!t.getRequestURI().toASCIIString().contains("?=")) {
            ArrayList<String> redir = new ArrayList<>();
            redir.add("quiz?=1");
            t.getResponseHeaders().put("Location", redir);
            t.sendResponseHeaders(302, -1);
            t.close();
        }

        String currentQuestion = t.getRequestURI().toASCIIString().split("=")[1];
        int currQ = Integer.parseInt(currentQuestion);
        Quiz q = QuizManager.INSTANCE.getCurrentQuiz(UserManager.INSTANCE.getUser(user));

        // QUIZ BANK COMMUNICATION

        int attempts = q.getNumberOfAttempts(currQ);

        q.setNumberOfAttempts(currQ, attempts - 1);
        q.save();

        ArrayList<String> redirect = new ArrayList<>();
        redirect.add(t.getRequestURI().toString());
        t.getResponseHeaders().put("Location", redirect);
        t.sendResponseHeaders(302, -1);
        t.close();
    }
}
