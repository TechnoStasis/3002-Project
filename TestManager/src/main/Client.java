import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException {
        String serverHostname = "192.168.0.2";  //needs to know servers ip in order to be able to do anything
        int serverPort = 1234;

        Socket clientSocket = new Socket(serverHostname, serverPort);
        System.out.println("COnnected to server: " + clientSocket); //testing what server connected to

        InputStream inputStream = clientSocket.getInputStream();     
        byte[] syn = new byte[1]; //client sends Syn
        inputStream.read(syn);
 
        if (syn[0] == 0x01){  //test if client got a syn
            System.out.println("SYN recieved");

            OutputStream outputStream = clientSocket.getOutputStream(); //send syn-ack to server
            byte[] synAck = {0x02};
            outputStream.write(synAck);
            System.out.println("SYN-ACK sent");

            byte[] ack = new byte[1]; //client recieves ack
            inputStream.read(ack);
            if (ack[0] == 0x03){
                System.out.println("ACK recieved");

                String message = "Hello from the Client!, all acknlowedgments went through!";  //testing writing message to server
                byte[] messageBytes = message.getBytes();
                outputStream.write(messageBytes);

                byte[] responseBytes = new byte[1024];  //testing recieveing data from server
                int bytesRead = inputStream.read(responseBytes);
                String response = new String(responseBytes,0, bytesRead);
                System.out.println("Recieved message from server: " + response);

                clientSocket.close();

            }
            else{
                System.out.println("Failed to recieve ACK");
            }
        }
        else{
            System.out.println("Failed to recieve SYN");
        }
    }
}