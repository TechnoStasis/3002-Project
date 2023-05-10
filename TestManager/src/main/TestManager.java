package main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import main.page.AbstractPageHandler;
import main.page.LoginPage;
import main.page.ProfilePage;
import main.page.QuizPage;
import main.page.RegisterPage;

public class TestManager {

	public static final String jarPath = TestManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();

	private static void registerContext(HttpServer s) {
		s.createContext("/login", new LoginPage());
		s.createContext("/logout", new AbstractPageHandler() {

			@Override
			public void handleGet(HttpExchange t) throws IOException {

				ArrayList<String> cookies = new ArrayList<>();
				if (t.getRequestHeaders().get("Cookie") != null) {
					for (String str : t.getRequestHeaders().get("Cookie")) {
						cookies.add(str + "; Max-Age=-1");
					}
				}
				t.getResponseHeaders().put("Set-Cookie", cookies);

				ArrayList<String> redirect = new ArrayList<String>();
				redirect.add("login");
				t.getResponseHeaders().put("Location", redirect);
				t.sendResponseHeaders(302, -1);
				t.close();
			}

			@Override
			public void handlePost(HttpExchange t) throws IOException {
			}

		});
		s.createContext("/register", new RegisterPage());

		s.createContext("/profile", new ProfilePage());
		s.createContext("/quiz", new QuizPage());
	}

	public static void main(String[] args) {

		try {
			InetSocketAddress a = new InetSocketAddress("localhost", 8081);

			HttpServer server = HttpServer.create(a, 0);
			ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

			registerContext(server);
			server.setExecutor(threadPoolExecutor);
			server.start();
			System.out.println("Starting server at " + a.getHostName() + ":" + a.getPort());

		} catch (Exception e) {
			System.out.println("Failed to initialize server");
			e.printStackTrace();
		}
	}
}