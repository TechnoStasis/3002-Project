package main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ProtocolManager {
    
    public static final ProtocolManager INSTANCE = new ProtocolManager();

    private ProtocolManager()
    {
        try {
            createListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private void createListener() throws IOException
	{
		   // Get selector
		   Selector selector = Selector.open();
 
		   System.out.println("Selector open: " + selector.isOpen());
	
		   // Get server socket channel and register with selector
		   ServerSocketChannel serverSocket = ServerSocketChannel.open();
		   InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5454);
		   serverSocket.bind(hostAddress);
		   serverSocket.configureBlocking(false);
		   int ops = serverSocket.validOps();
		   SelectionKey selectKy = serverSocket.register(selector, ops, null);
	
		   for (;;) {
	
			   System.out.println("Waiting for select...");
			   int noOfKeys = selector.select();
	
			   System.out.println("Number of selected keys: " + noOfKeys);
	
			   Set selectedKeys = selector.selectedKeys();
			   Iterator iter = selectedKeys.iterator();
	
			   while (iter.hasNext()) {
	
				   SelectionKey ky = (SelectionKey) iter.next();
	
				   if (ky.isAcceptable()) {
	

					   // Accept the new client connection
					   SocketChannel client = serverSocket.accept();
					   client.configureBlocking(false);
   
					   client.write(ByteBuffer.wrap("Hi".getBytes()));
					   // Add the new connection to the selector
					   client.register(selector, SelectionKey.OP_READ);
	
					   System.out.println("Accepted new connection from client: " + client);
				   }
				   else if (ky.isReadable()) {
	
					   // Read the data from client
	
					   SocketChannel client = (SocketChannel) ky.channel();
					   ByteBuffer buffer = ByteBuffer.allocate(256);
					   client.read(buffer);
					   String output = new String(buffer.array()).trim();
	
					   System.out.println("Message read from client: " + output);
		
					   if (output.equals("Bye.")) {
	
						   client.close();
						   System.out.println("Client messages are complete; close.");
					   }
	
				   } // end if (ky...)
	
				   iter.remove();
	
			   } // end while loop
	
		   } // end for loop
	}
}
