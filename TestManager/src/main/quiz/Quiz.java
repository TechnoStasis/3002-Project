package main.quiz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Quiz {
    File file;

    String type;
    Question[] questions = new Question[10];

    public Quiz(File path) throws FileNotFoundException, IOException {
        this.file = path;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int index = 0;
            while ((line = br.readLine()) != null) {

                if (line.contains("type:")) {
                    type = line.split(":")[1].replace(" ", "");
                }

                if (line.contains("q")) {
                    String attempts = line.split(":")[1];
                    char correct = line.split(":")[2].toCharArray()[0];
                    int cor = Integer.parseInt(correct + "");
                    String id = line.split(":")[3].replace("\n", "");
                    boolean corr = cor > 0;
                    questions[index] = new Question(Integer.parseInt(attempts), corr, id);

                    index++;
                }

                if (index > 10)
                    break;
            }
        }
    }

    public void save() throws IOException {
        FileWriter f = new FileWriter(file);

        f.write("type:" + type + "\n");
        for (int i = 0; i < questions.length; i++) {
            f.write("q" + (i + 1) + ":" + questions[i].attemptsLeft + ":" + (questions[i].correct ? 0 : 1));
            f.write(":" + questions[i].id + "\n");
        }
        f.close();
    }

    public String toString() {
        String str = "type: " + type + "\n";
        for (int i = 0; i < questions.length; i++)
            str = str + i + ": " + questions[i].toString() + "\n";
        return str;
    }

    public String getQuestionId(int question)
    {
        return questions[question-1].id;
    }

    public int getNumberOfAttempts(int question) {
        return questions[question - 1].attemptsLeft;
    }

    public void setNumberOfAttempts(int question, int attempts) {
        questions[question - 1].attemptsLeft = attempts;
    }

    public String getPath() {
        return this.file.getName().replace(".txt", "");
    }

    public int totalMarks() {
        int marks = 0;
        for (int i = 0; i < questions.length; i++)
            if (questions[i].correct)
                marks = marks + questions[i].attemptsLeft;
        return marks;
    }

    class Question {
        int attemptsLeft;
        boolean correct;
        String id;
        public Question(int a, boolean co, String id) {
            attemptsLeft = a;
            correct = co;
            this.id = id;
        }

        @Override
        public String toString() {
            return "{id: " + id + ", attempts remaining: " + attemptsLeft + ", correctness: " + correct + "}";
        }
    }

    public String getType() {
        return type;
    }

}
