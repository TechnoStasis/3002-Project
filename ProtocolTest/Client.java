import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException {
        String serverHostname = "192.168.0.14";  //needs to know servers ip in order to be able to do anything
        int serverPort = 1234;


        try(Socket clientSocket = new Socket(serverHostname, serverPort)){

            InputStream input = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = clientSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(output,true);

            //Create file writer
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter("serverdata.txt"));

            //Send SYN-ACK to server
            writer.println((char)0X02);
            System.out.println("SYN-ACK sent");

            //Recieve ACK from server
            int ack = reader.read();
            if (ack == 0x03) {
                System.out.println("ACK recieved");
                

                //Loop to receive questions
                String recievedData;
                while ((recievedData = reader.readLine()) != null){
                    System.out.println("Recieved data: " + recievedData);

                    fileWriter.write(recievedData);
                    fileWriter.newLine();

                    // Send ACK for recieved data
                    writer.println((char)0x04);
                    System.out.println("ACH sent for data");
                }

                //Close file writer
                fileWriter.close();


            } else {
                System.out.println("Failed to receive ACK");
            }
        } catch (UnknownHostException ex){
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex){
            System.out.println("I/O error: " + ex.getMessage());
        }     
        
    }
}