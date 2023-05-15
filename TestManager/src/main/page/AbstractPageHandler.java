package main.page;

import java.io.IOException;
import java.util.ArrayList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class AbstractPageHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("GET"))
            handleGet(t);
        ;
        if (t.getRequestMethod().equals("POST"))
            handlePost(t);
        ;
    }

    public abstract void handleGet(HttpExchange t) throws IOException;

    public abstract void handlePost(HttpExchange t) throws IOException;

    public static final void redirect(HttpExchange t, String location) throws IOException {
        ArrayList<String> redir = new ArrayList<>();
        redir.add(location);
        t.getResponseHeaders().put("Location", redir);
        t.sendResponseHeaders(302, -1);
        t.close();
    }

}
