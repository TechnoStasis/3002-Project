package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import main.TestManager.Pair;

public class ProtocolMethods {

	private volatile boolean isBlocking = false;

    public boolean isBlocking() {
        return this.isBlocking;
    }
    

    /**
     * 
     * @param serverHostname
     * @param serverPort
     * @param questionNumber
     * @param language       C for C, P for Python
     * @return
     */
    public static int[] getQuestionIds(String qb, int questionNumber, String language) {

        Pair address = TestManager.accessPoints.get(qb);
        String serverHostname = address.left;
        int serverPort = Integer.parseInt(address.right);
        try (Socket clientSocket = new Socket(serverHostname, serverPort);
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream()) {
            // System.out.println("Connected to server: " + clientSocket); // Debugging line

            // When sending QF Request
            String request = "QF";
            outputStream.write(request.getBytes());
            byte[] ack = new byte[1];
            if (inputStream.read(ack) != -1 && ack[0] == 0x03) { // ACK received for QF request
                String payload = questionNumber + "$" + language; // Q number and language of question separated by $
                outputStream.write(payload.getBytes());
                if (inputStream.read(ack) != -1 && ack[0] == 0x04) { // ACK received for question number and language
                                                                     // request

                    boolean correctHash = false;
                    String received_q_ids = null;
                    // int error = 0; // un comment to simulate wrong hash

                    while (!correctHash) {
                        List<Byte> buffer = new ArrayList<>();
                        int bytesRead;
                        byte[] tempBuffer = new byte[1024];

                        while ((bytesRead = inputStream.read(tempBuffer)) != -1) {
                            for (int i = 0; i < bytesRead + 1; i++) {
                                buffer.add(tempBuffer[i]);
                                if (tempBuffer[i] == 64) // '@' means the connection is properly sent
                                {
                                    break;
                                }
                            }
                            if (tempBuffer[bytesRead - 1] == 64) {
                                break;
                            }
                        }

                        byte[] recievedBytes = new byte[buffer.size()];
                        for (int i = 0; i < buffer.size(); i++) {
                            recievedBytes[i] = buffer.get(i);
                        }

                        String receivedMessage = new String(recievedBytes, "UTF-8");

                        String[] splitData = receivedMessage.toString().split(" ", 2);
                        String receivedHash = splitData[0];
                        received_q_ids = splitData[1];
                        if (received_q_ids.length() > 0) { // want to remove the '@' at the end of the string
                            received_q_ids = received_q_ids.substring(0, received_q_ids.length() - 1);
                            System.out.println(received_q_ids);
                        }

                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] encodedHash = digest.digest(received_q_ids.getBytes(StandardCharsets.UTF_8));
                        String calculatedHash = bytesToHex(encodedHash);
                        // System.out.println(error); //Add error > 0 in if statement to simulate false
                        // hash

                        if (calculatedHash.equals(receivedHash)) {
                            correctHash = true;
                            byte[] dataAck = { 0x04 };
                            outputStream.write(dataAck);
                            String[] questionIdsArray = received_q_ids.split(",");
                            int[] questionIds = new int[questionIdsArray.length];
                            for (int i = 0; i < questionIdsArray.length; i++) {
                                String extractedNumberString = questionIdsArray[i].replaceAll("\\D+", ""); // Ensure
                                                                                                           // numbers
                                                                                                           // only
                                questionIds[i] = Integer.parseInt(extractedNumberString);
                            }

                            int receivedChar;
                            while ((receivedChar = inputStream.read()) != -1) {
                                char character = (char) receivedChar;
                                if (character == '@') {
                                    byte[] endDataAck = { 0x05 };
                                    outputStream.write(endDataAck);
                                    break; // Break out of the loop when the end of data is received
                                }
                            }

                            outputStream.close();
                            inputStream.close();
                            clientSocket.close();
                            return questionIds;
                        } else {
                            // error++;
                            byte[] incorrect = { 0x10 };
                            outputStream.write(incorrect);
                        }
                    }
                }
            } else {
                Thread.sleep(2000);
                outputStream.write(request.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 
     * @param serverHostname
     * @param serverPort
     * @param question_number
     * @param Option          TXT or DS, whether it's an answer or the text
     *                        description of the question
     * @return
     */
    public static String getQuestion(String qb, int question_number, String Option) {
        Pair address = TestManager.accessPoints.get(qb);
        String serverHostname = address.left;
        int serverPort = Integer.parseInt(address.right);
        try (Socket clientSocket = new Socket(serverHostname, serverPort);
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream()) {

            outputStream.write(Option.getBytes());
            byte[] ack = new byte[1];
            inputStream.read(ack);
            if (ack[0] != 0x03) {
                Thread.sleep(2000);
                outputStream.write(Option.getBytes());
                inputStream.read(ack);
            }

            String q_id = Integer.toString(question_number);
            outputStream.write(q_id.getBytes());
            inputStream.read(ack);
            if (ack[0] != 0x04) {
                return null; // method dies maybe want to change?
            } else {
                System.out.println("Acknowledgement for question number request received");
            }

            boolean correctHash = false;
            String receivedQuestion = null;

            while (!correctHash) {
                List<Byte> buffer = new ArrayList<>();
                int bytesRead;
                byte[] tempBuffer = new byte[1024];
                while ((bytesRead = inputStream.read(tempBuffer)) != -1) {
                    for (int i = 0; i < bytesRead + 1; i++) {
                        buffer.add(tempBuffer[i]);
                        if (tempBuffer[i] == 64) // '@' means the connection is properly sent
                        {
                            break;
                        }

                    }

                    if (tempBuffer[bytesRead - 1] == 64) {
                        break;
                    }
                }

                byte[] recievedBytes = new byte[buffer.size()];
                for (int i = 0; i < buffer.size(); i++) {
                    recievedBytes[i] = buffer.get(i);
                }

                String receivedMessage = new String(recievedBytes, "UTF-8");
                String[] splitData = receivedMessage.toString().split(" ", 2);
                String receivedHash = splitData[0];
                receivedQuestion = splitData[1];
                if (receivedQuestion.length() > 0) { // want to remove the '@' at the end of the string
                    receivedQuestion = receivedQuestion.substring(0, receivedQuestion.length() - 1);
                }

                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] encodedHash = digest.digest(receivedQuestion.getBytes(StandardCharsets.UTF_8));
                String calculatedHash = bytesToHex(encodedHash);

                if (calculatedHash.equals(receivedHash)) {
                    correctHash = true;
                    outputStream.write(new byte[] { 0x05 });

                    int receivedChar;
                    while ((receivedChar = inputStream.read()) != -1) {
                        char character = (char) receivedChar;
                        if (character == '@') {
                            byte[] endDataAck = { 0x06 };
                            outputStream.write(endDataAck);
                        }
                        break;
                    }

                    outputStream.close();
                    inputStream.close();
                    clientSocket.close();
                    return receivedQuestion;

                } else {
                    byte[] incorrect = { 0x10 };
                    outputStream.write(incorrect);
                }
            }
        } catch (IOException | NoSuchAlgorithmException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 
     * @param serverHostname
     * @param serverPort
     * @param answer
     * @param q_id
     * @param q_type         2 = C 3 = python
     * @return True if it's a correct answer or False if not
     */
    public static String markQuestion(String qb, String answer, int q_id, int q_type) {
        Pair address = TestManager.accessPoints.get(qb);
        String serverHostname = address.left;
        int serverPort = Integer.parseInt(address.right);

        try (Socket clientSocket = new Socket(serverHostname, serverPort);
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream()) {

            outputStream.write("MK".getBytes());
            byte[] ack = new byte[1];
            inputStream.read(ack);
            if (ack[0] != 0x03) {
                System.out.println("Acknowledgement for MK request not received");
                return null;
            }

            String command = answer + "$" + q_type + "$" + q_id;
            byte[] commandBytes = command.getBytes(StandardCharsets.UTF_8);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(commandBytes);
            byte[] hashCheck = bytesToHex(hashedBytes).getBytes(StandardCharsets.UTF_8);

            byte[] payload = new byte[hashCheck.length + 1 + commandBytes.length];
            System.arraycopy(hashCheck, 0, payload, 0, hashCheck.length);
            payload[hashCheck.length] = ' ';
            System.arraycopy(commandBytes, 0, payload, hashCheck.length + 1, commandBytes.length);

            outputStream.write(payload);
            outputStream.flush();

            boolean acknowledged = false;
            while (!acknowledged) {
                inputStream.read(ack);
                if (ack[0] == 0x04) {
                    acknowledged = true;
                } else {
                    Thread.sleep(2000);
                    outputStream.write(payload);
                }
            }
            boolean correctHash = false;
            String receivedQuestion = null;
            // int error = 0; //uncomment to simulate wrong hash

            while (!correctHash) {
                List<Byte> buffer = new ArrayList<>();
                int bytesRead;
                byte[] tempBuffer = new byte[1024];

                while ((bytesRead = inputStream.read(tempBuffer)) != -1) {
                    for (int i = 0; i < bytesRead + 1; i++) {
                        buffer.add(tempBuffer[i]);
                        if (tempBuffer[i] == 64) // '@' means connection fully sent , some funky case
                        {
                            break;
                        }
                    }

                    if (tempBuffer[bytesRead - 1] == 64) {
                        break;
                    }
                }
                byte[] recievedBytes = new byte[buffer.size()];
                for (int i = 0; i < buffer.size(); i++) {
                    recievedBytes[i] = buffer.get(i);
                }
                String receivedMessage = new String(recievedBytes, "UTF-8");
                String[] splitData = receivedMessage.toString().split(" ", 2);
                String receivedHash = splitData[0];
                receivedQuestion = splitData[1];

                if (receivedQuestion.length() > 0) {
                    receivedQuestion = receivedQuestion.substring(0, receivedQuestion.length() - 1);
                }

                MessageDigest recieved_digest = MessageDigest.getInstance("SHA-256");
                byte[] encodedHash = recieved_digest.digest(receivedQuestion.getBytes(StandardCharsets.UTF_8));
                String calculatedHash = bytesToHex(encodedHash);
                if (calculatedHash.equals(receivedHash)) {
                    correctHash = true;
                    outputStream.write(new byte[] { 0x05 });

                    int receivedChar;
                    while ((receivedChar = inputStream.read()) != -1) {
                        char character = (char) receivedChar;
                        if (character == '@') {
                            byte[] endDataAck = { 0x06 };
                            outputStream.write(endDataAck);
                            break; // have to break outta loop
                        }
                    }

                    outputStream.close();
                    inputStream.close();
                    clientSocket.close();

                    return receivedQuestion;
                } else {
                    // error++;
                    byte[] incorrect = { 0x10 };
                    outputStream.write(incorrect);
                }
            }
        } catch (IOException | NoSuchAlgorithmException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
