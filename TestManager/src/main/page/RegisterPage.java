package main.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

import main.HtmlRenderer;

public class RegisterPage extends AbstractPageHandler {

    String htmlPage;

    public RegisterPage() {
        htmlPage = HtmlRenderer.readHTML("register.html");
    }

    @Override
    public void handleGet(HttpExchange t) throws IOException {
        OutputStream io = t.getResponseBody();
        t.sendResponseHeaders(200, htmlPage.length());
        io.write(htmlPage.getBytes());
        io.close();
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
        String confirm = details[2].split("=")[1];

        System.out.println(username);
        System.out.println(password);
        System.out.println(confirm);
    }

}
