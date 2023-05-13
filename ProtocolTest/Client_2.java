import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Client_2 {
    public static void main(String[] args) throws IOException {
        String serverHostname = "192.168.0.14";
        int serverPort = 1234;

        Socket clientSocket = new Socket(serverHostname, serverPort);
        System.out.println("Connected to server: " + clientSocket);

        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();
        
        byte[] syn = new byte[1];
        inputStream.read(syn);

        if (syn[0] == 0x01) {
            System.out.println("SYN received");

            byte[] synAck = {0x02};
            outputStream.write(synAck);
            System.out.println("SYN-ACK sent");

            byte[] ack = new byte[1];
            inputStream.read(ack);
            if (ack[0] == 0x03) {
                System.out.println("ACK received");

                BufferedWriter fileWriter = new BufferedWriter(new FileWriter("serverdata.txt"));

                StringBuilder question = new StringBuilder();
                int receivedChar;

                while ((receivedChar = inputStream.read()) != -1) {
                    char character = (char) receivedChar;
                    if (character == '#') {
                        question.append(character);
                        System.out.println("Received data: " + question.toString());

                        // Split the received data into hash and question
                        String[] splitData = question.toString().split(" ", 2);
                        String receivedHash = splitData[0];
                        String actualQuestion = splitData[1];

                        try {

                            MessageDigest digest = MessageDigest.getInstance("SHA-256");
                            // Question string coverted back into bytes to create a byte array of computed hash
                            byte[] encodedhash = digest.digest(
                                actualQuestion.getBytes(StandardCharsets.UTF_8));
                            // convert into hexadecimal string
                            String calculatedHash = bytesToHex(encodedhash);

                            if (calculatedHash.equals(receivedHash)) {
                                fileWriter.write(actualQuestion);
                                fileWriter.newLine();
                                System.out.println("Nice it was the correct hash, correct data");

                                byte[] dataAck = {0x04};
                                outputStream.write(dataAck);
                                System.out.println("ACK sent for data");
                            } else {
                                System.out.println("Hash mismatch for received data. Data may be corrupted.");
                            }

                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }

                        question.setLength(0); // Clear the question
                    } else if (character == '@'){
                        System.out.println("Received end of data ");
                        byte[] endDataAck = {0x05};
                        outputStream.write(endDataAck);
                        System.out.println("ACK sent for end of data");
                        break;
                    } else {
                        question.append(character);
                    }
                }

                byte[] end_ack = {0x05};
                outputStream.write(end_ack);
                System.out.println("Data in text file successfully received and stored");

                fileWriter.close();
                clientSocket.close();
            } else {
                System.out.println("Failed to receive ACK");
            }
        } else {
            System.out.println("Failed to receive SYN");
        }
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
}
