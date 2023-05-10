package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import users.User;

public class QuizManager {

    public static final QuizManager INSTANCE = new QuizManager(
            "/Users/yvesreyes/Documents/3002-Project/TestManager/assets/quiz/");

    private final String quizPath;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
