package main.quiz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @authors 22887893 YVES MIGUEL REYES 33.3%
 * @authors 23262446 SRINIKETH KARLAPUDI 33.3%
 * @authors 23468614 CHENG LI 33.3%
 */
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
                    questions[index] = new Question(Integer.parseInt(attempts), corr, id, index + 1);

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
            f.write("q" + (i + 1) + ":" + questions[i].attemptsLeft + ":" + (questions[i].correct ? 1 : 0));
            f.write(":" + questions[i].id + "\n");
            questions[i].save();
        }
        f.close();
    }

    public String toString() {
        String str = "type: " + type + "\n";
        for (int i = 0; i < questions.length; i++)
            str = str + i + ": " + questions[i].toString() + "\n";
        return str;
    }

    public String getQuestionId(int question) {
        return questions[question - 1].id;
    }

    public String getAnswer(int question) {
        return questions[question - 1].answer;
    }

    public int getNumberOfAttempts(int question) {
        return questions[question - 1].attemptsLeft;
    }

    public boolean getCorrect(int question) {
        return questions[question - 1].correct;
    }

    public void setCorrect(int question) {
        questions[question - 1].correct = true;
    }

    public void setIncorrect(int question) {
        questions[question - 1].correct = false;
    }

    public void setAnswer(int question, String answer) {
        questions[question - 1].answer = answer;
    }

    public void setNumberOfAttempts(int question, int attempts) {
        questions[question - 1].attemptsLeft = attempts;
    }

    public String getPath() {
        return this.file.getName().replace(".txt", "");
    }

    public String getType() {
        return type;
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
        File questionPath;
        String answer = "";

        public Question(int a, boolean co, String id, int number) {
            questionPath = new File(file.getAbsolutePath().replace(".txt", "/" + number + ".txt"));
            new File(questionPath.getParent()).mkdirs();
            if (!questionPath.exists())
                try {
                    questionPath.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            else
                try (BufferedReader br = new BufferedReader(new FileReader(questionPath))) {
                    String line;
                    while (((line = br.readLine()) != null)) {
                        answer = answer + "\n" + line;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            attemptsLeft = a;
            correct = co;
            this.id = id;
        }

        @Override
        public String toString() {
            return "{id: " + id + ", attempts remaining: " + attemptsLeft + ", correctness: " + correct + "}";
        }

        public void save() throws IOException {
            FileWriter f = new FileWriter(questionPath);
            String newLineChar = System.getProperty("line.separator");
            answer = answer.replace("\n", newLineChar);
            f.write(answer);
            f.close();
        }
    }
}
