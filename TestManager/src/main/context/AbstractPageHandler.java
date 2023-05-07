package main.context;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class AbstractPageHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        if(t.getRequestMethod().equals("GET")) handleGet(t);;
        if(t.getRequestMethod().equals("POST")) handlePost(t);;
    }

    public abstract void handleGet(HttpExchange t) throws IOException;
    public abstract void handlePost(HttpExchange t) throws IOException;
       
}
