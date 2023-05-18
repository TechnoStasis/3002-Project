package main;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;

public class ProtocolMethods {

    public static int[] get_question_ids(String serverHostname, int serverPort, int questionNumber, String language) {
        try (Socket clientSocket = new Socket(serverHostname, serverPort);
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream()) {
            System.out.println("Connected to server: " + clientSocket); // Debugging line

            // When sending QF Request
            String request = "QF";
            outputStream.write(request.getBytes()); // Send request to server
            byte[] ack = new byte[1];
            if (inputStream.read(ack) != -1 && ack[0] == 0x03) { // ACK received for QF request
                String payload = questionNumber + "#" + language; // Q number and language of question separated by #
                outputStream.write(payload.getBytes());
                if (inputStream.read(ack) != -1 && ack[0] == 0x04) { // ACK received for question number and language
                                                                     // request
                    byte[] buffer = new byte[1024];
                    int bytesRead = inputStream.read(buffer);

                    if (bytesRead > 0) {
                        String receivedMessage = new String(buffer, 0, bytesRead);
                        String[] splitData = receivedMessage.split(" ", 2);
                        String receivedHash = splitData[0];
                        String received_q_ids = splitData[1];

                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] encodedHash = digest.digest(received_q_ids.getBytes(StandardCharsets.UTF_8));
                        String calculatedHash = bytesToHex(encodedHash);

                        if (calculatedHash.equals(receivedHash)) {
                            System.out.println("Nice, it was the correct hash, correct data");
                            byte[] dataAck = { 0x04 };
                            outputStream.write(dataAck);
                            System.out.println("ACK sent for data");

                            String[] questionIdsArray = received_q_ids.split(",");
                            int[] questionIds = new int[questionIdsArray.length];
                            for (int i = 0; i < questionIdsArray.length; i++) {
                                String extractedNumberString = questionIdsArray[i].replaceAll("\\D+", "");
                                questionIds[i] = Integer.parseInt(extractedNumberString);
                            }

                            int receivedChar;
                            while ((receivedChar = inputStream.read()) != -1) {
                                char character = (char) receivedChar;
                                if (character == '@') {
                                    byte[] endDataAck = { 0x05 };
                                    outputStream.write(endDataAck);
                                    System.out.println("Ack sent for end of data");
                                    break; // Break out of the loop when the end of data is received
                                }
                            }

                            outputStream.close();
                            inputStream.close();
                            clientSocket.close();
                            return questionIds;
                        } else {
                            System.out.println("Hash mismatch");
                        }
                    }
                }
            } else {
                Thread.sleep(2000);
                System.out.println("Acknowledgement for QF request not received, re-sending");
                outputStream.write(request.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getQuestion(String serverHostname, int serverPort, int question_number, String Option) {// when
                                                                                                                 // wanting
                                                                                                                 // question
                                                                                                                 // or
                                                                                                                 // answer
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
                if (ack[0] != 0x03) {
                    System.out.println("Acknowledgement for TXT request not received, re-sending");
                    return null;
                }
            }

            String q_id = Integer.toString(question_number);
            outputStream.write(q_id.getBytes());
            inputStream.read(ack);
            if (ack[0] != 0x04) {
                System.out.println("Acknowledgement for question number request not received");
                return null;
            } else {
                System.out.println("Acknowledgement for question number request received");
            }

            // int bufferSize = 1024;
            // byte[] buffer = new byte[bufferSize];
            // int bytesRead;
            // StringBuilder stringBuilder = new StringBuilder();

            // while ((bytesRead = inputStream.read(buffer)) != -1) {
            // stringBuilder.append(new String(buffer, 0, bytesRead,
            // StandardCharsets.UTF_8));
            // System.out.println(bytesRead);
            // }

            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);

            String receivedMessage = new String(buffer, 0, bytesRead); // jank method , put a proper check
            // System.out.println("The built string is " + receivedMessage);

            String[] splitData = receivedMessage.toString().split(" ", 2);
            String receivedHash = splitData[0];
            String receivedQuestion = splitData[1];

            // System.out.println(receivedQuestion);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(receivedQuestion.getBytes(StandardCharsets.UTF_8));
            String calculatedHash = bytesToHex(encodedHash);

            if (calculatedHash.equals(receivedHash)) {
                System.out.println("Nice it was the correct hash, correct data");
                outputStream.write(new byte[] { 0x05 });

                int receivedChar;
                while ((receivedChar = inputStream.read()) != -1) {
                    char character = (char) receivedChar;
                    if (character == '@') {
                        byte[] endDataAck = { 0x06 };
                        outputStream.write(endDataAck);
                        System.out.println("Ack sent for end of data");
                    }
                }

                outputStream.close();
                inputStream.close();
                clientSocket.close();

                return receivedQuestion;
            } else {
                System.out.println("Hash mismatch");
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

    

    public static String Mark_Question(String serverHostname, int serverPort, String answer, int q_id, int q_type) { // WHen wanting True or False for question mark
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
    
            String command = answer + "#" + q_id + "#" + q_type;
            System.out.println(command);
            byte[] commandBytes = command.getBytes(StandardCharsets.UTF_8);
    
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(commandBytes);
            byte[] hashCheck = bytesToHex(hashedBytes).getBytes(StandardCharsets.UTF_8);
    
            byte[] payload = new byte[hashCheck.length + 1 + commandBytes.length];
            System.arraycopy(hashCheck, 0, payload, 0, hashCheck.length);
            payload[hashCheck.length] = ' ';
            System.arraycopy(commandBytes, 0, payload, hashCheck.length + 1, commandBytes.length);

            //Debugging
         //   String payloadString = new String(payload, StandardCharsets.UTF_8);
          //  System.out.println(payloadString);
            
    
            outputStream.write(payload);
            outputStream.flush();
            System.out.println(payload);
            System.out.println("SEnt payload already");
    
            boolean acknowledged = false;
            while (!acknowledged ) {
                inputStream.read(ack);
                if (ack[0] == 0x04) {
                    System.out.println("Payload acknowledged");
                    acknowledged = true;
                } else {
                    Thread.sleep(2000);
                    System.out.println("Acknowledgement for payload not received, re-sending");
                    outputStream.write(payload);
                }
            }

         //   int bufferSize = 1024;
          //  byte[] buffer = new byte[bufferSize];
          //  int bytesRead;
           // StringBuilder stringBuilder = new StringBuilder();
           // System.out.println("This is just before its stuck");


            //while ((bytesRead = inputStream.read(buffer)) != -1) {
            //    stringBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            //}

            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            String receivedMessage = new String(buffer, 0, bytesRead); 
            
            System.out.println("THis is where it is stuck?");
            String[] splitData = receivedMessage.toString().split(" ", 2);
            String receivedHash = splitData[0];
            String receivedQuestion = splitData[1];
    
            MessageDigest recieved_digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = recieved_digest.digest(receivedQuestion.getBytes(StandardCharsets.UTF_8));
            String calculatedHash = bytesToHex(encodedHash);
            System.out.println("Got to just before calculating the hash");

            if (calculatedHash.equals(receivedHash)) {
                System.out.println("Nice it was the correct hash, correct data");
                outputStream.write(new byte[]{0x05});
    
    
                int receivedChar;
                while ((receivedChar = inputStream.read()) != -1){
                    char character = (char) receivedChar;
                    if (character == '@')
                        {
                        byte[] endDataAck = {0x06};
                        outputStream.write(endDataAck);
                        System.out.println("Ack sent for end of data");
                        break; //have to break outta loop
                        }
                }
    
                outputStream.close();
                inputStream.close();
                clientSocket.close();
    
                return receivedQuestion;
            } else {
                System.out.println("Hash mismatch");
            }
        } catch (IOException | NoSuchAlgorithmException | InterruptedException e) {
            e.printStackTrace();
        }
    
        return null;
    }

}
