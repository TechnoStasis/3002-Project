package main.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.sun.net.httpserver.HttpExchange;

import main.CommandList;
import main.HtmlHelper;
import main.ProtocolMethods;
import main.QuizManager;
import main.ThreadDirector;
import main.UserManager;
import main.quiz.Quiz;
import main.users.User;

/**
 * @authors 22887893 YVES MIGUEL REYES 33.3%
 * @authors 23262446 SRINIKETH KARLAPUDI 33.3%
 * @authors 23468614 CHENG LI 33.3%
 */
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
        String answer = q.getAnswer(cQ);
        String quizType = q.getType();
        boolean correct = q.getCorrect(cQ);

        HashMap<String, Object> data = new HashMap<>();
        data.put("questionnumber", currentQuestion);
        data.put("attempts", attempts + "");

        ThreadDirector threadDirector = new ThreadDirector();
        Queue<CommandList> commandDump1 = new LinkedList<>();
        String questionText = null;

        if (threadDirector.isBlocking()) {
            CommandList cmd = new CommandList(quizType, Integer.parseInt(id), "TXT");
            commandDump1.add(cmd);

            while (true) {
                if (!threadDirector.isBlocking()) {
                    CommandList obj = commandDump1.poll();
                    questionText = ProtocolMethods.getQuestion(obj.getcmd1(), obj.getcmd2(), obj.getcmd3());
                    break;
                }
            }
        } else {

            questionText = ProtocolMethods.getQuestion(quizType, Integer.parseInt(id), "TXT");

        }

        if (!correct && attempts <= 0)
            questionText = HtmlHelper.appendError(questionText);

        data.put("question", questionText);
        data.put("answer", answer);
        if (!correct)
            data.put("button",
                    attempts > 0 ? HtmlHelper.createButton("Submit") : HtmlHelper.appendError("No More Attempts"));
        else
            data.put("button", HtmlHelper.appendGreen("You got it right!"));

        if (correct || attempts <= 0) {
            String correctAnswer = ProtocolMethods.getQuestion(quizType, Integer.parseInt(id), "DS");
            data.put("correctanswer", HtmlHelper.largeTextBoxTag(correctAnswer));
        } else
            data.put("correctanswer", "");

        data.put("finish", HtmlHelper.createSubmitButton("finish", "Finish and Submit"));

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
        String answer = bufferedReader.readLine();

        if (answer.contains("finish=")) {
            QuizManager.INSTANCE.removeActiveQuiz(user);
            redirect(t, "profile");

            return;
        }

        if (answer.contains("answer=") && answer.split("answer=").length > 1)
            answer = java.net.URLDecoder.decode(answer.split("answer=")[1], StandardCharsets.UTF_8.name());
        else
            answer = "";

        int currentQuestion = Integer.parseInt(t.getRequestURI().toASCIIString().split("=")[1]);
        Quiz q = QuizManager.INSTANCE.getCurrentQuiz(user);
        int qid = Integer.parseInt(q.getQuestionId(currentQuestion));
        q.setAnswer(currentQuestion, answer);

        String quizType = q.getType();

        int type = (currentQuestion == 10 ? (q.getType().equals("P") ? 3 : 2) : 1);
        boolean correctness = Boolean.parseBoolean(ProtocolMethods.markQuestion(quizType, answer, qid, type));

        if (!correctness) {
            int attempts = q.getNumberOfAttempts(currentQuestion);
            q.setNumberOfAttempts(currentQuestion, attempts - 1);
            q.setIncorrect(currentQuestion);
        } else {
            q.setCorrect(currentQuestion);
        }

        q.save();

        redirect(t, t.getRequestURI().toString());
    }
}
