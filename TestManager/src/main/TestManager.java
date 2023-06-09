package main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import main.page.AbstractPageHandler;
import main.page.LoginPage;
import main.page.ProfilePage;
import main.page.QuizPage;
import main.page.RegisterPage;


/**
 * @authors 22887893 YVES MIGUEL REYES 33.3%
 * @authors 23262446 SRINIKETH KARLAPUDI 33.3%
 * @authors 23468614 CHENG LI 33.3%
 */
public class TestManager {

	public static final String MASTER_PATH = TestManager.class.getProtectionDomain().getCodeSource().getLocation()
			.getPath();

	public static final HashMap<String, Pair> accessPoints = new HashMap();

	private static void registerContext(HttpServer s) {
		s.createContext("/login", new LoginPage());
		s.createContext("/", new LoginPage());
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

		if (args.length != 2) {
			System.out.println("Must provide 2 ip addresses for the Python and the C QB!");
			return;
		}

		String pythonAddress = args[0];
		String cAddress = args[1];

		accessPoints.put("P", new Pair(pythonAddress.split(":")[0], pythonAddress.split(":")[1]));
		accessPoints.put("C", new Pair(cAddress.split(":")[0], cAddress.split(":")[1]));
	
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

	static class Pair {
		String left, right;

		public Pair(String a, String b) {
			left = a;
			right = b;
		}

		@Override
		public String toString() {
			return left + ":" + right;
		}
	}

}
