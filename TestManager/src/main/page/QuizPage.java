package main.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.sun.net.httpserver.HttpExchange;

import main.HtmlRenderer;

public class QuizPage extends AbstractPageHandler {

    String htmlPage;

    public QuizPage() {
        htmlPage = HtmlRenderer.readHTML("quiz.html");
    }

    @Override
    public void handleGet(HttpExchange t) throws IOException {
        t.sendResponseHeaders(200, htmlPage.length());
        t.getResponseBody().write(htmlPage.getBytes());
        t.getResponseBody().close();
    }

    @Override
    public void handlePost(HttpExchange t) throws IOException {


        InputStream io = t.getRequestBody();
        InputStreamReader inputStreamReader = new InputStreamReader(io);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String req = bufferedReader.readLine().replace("+", " ");
        String[] details = req.split("&");

        System.out.println(req);


    }

}
