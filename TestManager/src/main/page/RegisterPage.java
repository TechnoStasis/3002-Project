package main.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

import main.HtmlRenderer;
import main.UserManager;
import users.User;

public class RegisterPage extends AbstractPageHandler {

    String htmlPage;

    public RegisterPage() {
        htmlPage = HtmlRenderer.readHTML("register.html");
    }

    @Override
    public void handleGet(HttpExchange t) throws IOException {
        OutputStream io = t.getResponseBody();
        HashMap<String, Object> hiddenError = new HashMap<>();
        hiddenError.put("error", "");
        String htmlRender = HtmlRenderer.render(htmlPage, hiddenError);
        t.sendResponseHeaders(200, htmlRender.length());
        io.write(htmlRender.getBytes());
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

        if(!password.equals(confirm)) {
            OutputStream os = t.getResponseBody();
            HashMap<String, Object> error = new HashMap<>();
            error.put("error", HtmlRenderer.appendError("Passwords don't match!"));
            String htmlRender = HtmlRenderer.render(htmlPage, error);
            t.sendResponseHeaders(200, htmlRender.length());
            os.write(htmlRender.getBytes());
            os.close();

            return;
        }

        if (UserManager.INSTANCE.validate(username, password)) {
            OutputStream os = t.getResponseBody();
            HashMap<String, Object> error = new HashMap<>();
            error.put("error", HtmlRenderer.appendError("User already exists!"));
            String htmlRender = HtmlRenderer.render(htmlPage, error);
            t.sendResponseHeaders(200, htmlRender.length());
            os.write(htmlRender.getBytes());
            os.close();
            
            return;
        }

        UserManager.INSTANCE.registerUser(new User(username, password));
        
        redirect(t, "login");
    }

}
