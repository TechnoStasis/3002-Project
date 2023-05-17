package main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ProtocolManager {

    public static final ProtocolManager INSTANCE = new ProtocolManager();

    private HashMap<String, SocketChannel> questionBanks;

    private ProtocolManager() {
        try {
            createListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createListener() throws IOException {

        // Creates new selector
        Selector selector = Selector.open();

        System.out.println("Selector open: " + selector.isOpen());

        // Get server socket channel and register with selector
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5454);
        serverSocket.bind(hostAddress);
        serverSocket.configureBlocking(false);
        int ops = serverSocket.validOps();
        SelectionKey selectKy = serverSocket.register(selector, ops, null);

        HashMap<String, SocketChannel> questionBanks = new HashMap<>();
        int MAX_CONNECTIONS = 1;

        while (questionBanks.size() < MAX_CONNECTIONS) {

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

                    // Add the new connection to the selector
                    client.register(selector, SelectionKey.OP_READ);

                    if (questionBanks.get("Hi") == null)
                        questionBanks.put("Hi", client);
                    else
                        questionBanks.put("Yo", client);

                    System.out.println("Accepted new connection from client: " + client);
                }
                iter.remove();
            }

        }

        selector.close();

        SocketChannel hi = questionBanks.get("Hi");
        hi.configureBlocking(true);
        hi.write(ByteBuffer.wrap("Holy".getBytes()));
    }
}
