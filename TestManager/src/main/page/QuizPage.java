package main.page;

import java.io.BufferedReader;
import java.io.FileReader;
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

        int attempts = q.getNumberOfAttempts(Integer.parseInt(currentQuestion));

        HashMap<String, Object> data = new HashMap<>();
        data.put("questionnumber", currentQuestion);
        data.put("attempts", attempts + "");

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

        int attempts = q.getNumberOfAttempts(currQ);

        q.setNumberOfAttempts(currQ, attempts - 1);
        q.save();
        
        t.sendResponseHeaders(200, -1);
        t.close();
    }

}
