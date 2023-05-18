package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import main.quiz.Quiz;
import main.users.User;

public class QuizManager {

    public static final QuizManager INSTANCE = new QuizManager(
            TestManager.MASTER_PATH + "assets/quiz/");

    private final String quizPath;

    private HashMap<String, Quiz> userquiz = new HashMap<>();

    public QuizManager(String string) {
        quizPath = string;

        Path path = Paths.get(quizPath);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createUserQuizPath(User user) {
        Path path = Paths.get(quizPath + "/" + user.getUsername().hashCode());
        try {
            Files.createDirectories(path);
            File file = getUserIndexFile(user);
            if (!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserQuizPath(User user) {
        return quizPath + "/" + user.getUsername().hashCode();
    }

    public File getUserIndexFile(User user) {
        return new File(getUserQuizPath(user) + "/" + "index.txt");
    }

    public void createNewQuiz(User user) {
        createUserQuizPath(user);

        String timestamp = LocalDateTime.now().toString();
        File f = new File(getUserQuizPath(user) + "/" + timestamp + ".txt");
        if (!f.exists())
            try {
                f.createNewFile();
                FileWriter file = new FileWriter(getUserIndexFile(user));
                file.write(timestamp);
                file.close();

                FileWriter quizFile = new FileWriter(f);
                quizFile.write("type:python\n"); //Get the type of the quiz aka which QB to call
                for (int i = 1; i <= 10; i++) {
                    quizFile.write("q" + i + ":3:0");
                    quizFile.write(":id" + "\n"); // Get the id for this specific question
                }
                quizFile.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public File getCurrentUserQuizFile(User user) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(getUserIndexFile(user)));
            String str;
            while ((str = in.readLine()) != null) {
                File f = new File(getUserQuizPath(user) + "/" + str + ".txt");
                in.close();
                return f;
            }
            if (str == null || str.isEmpty())
                return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Quiz getCurrentQuiz(User user) throws FileNotFoundException, IOException {
        if (userquiz.get(user.getUsername()) != null)
            return userquiz.get(user.getUsername());
        else if (getCurrentUserQuizFile(user) != null) {
            userquiz.put(user.getUsername(), new Quiz(getCurrentUserQuizFile(user)));
            return userquiz.get(user.getUsername());
        } else
            return null;
    }

    public ArrayList<Quiz> getPastQuizzes(User user) throws IOException {
        ArrayList<Quiz> pastQuizzes = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(getUserQuizPath(user)))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path) && !path.toString().contains("index.txt")) {
                    pastQuizzes.add(new Quiz(path.toFile()));
                }
            }
        }

        return pastQuizzes;
    }
}
