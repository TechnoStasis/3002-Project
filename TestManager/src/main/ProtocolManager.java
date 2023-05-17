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
            listenForQB();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToQB(String qb, String message) throws IOException {
        questionBanks.get(qb).write(ByteBuffer.wrap(message.getBytes()));
    }

    public String readFromQB(String qb) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(256);
        questionBanks.get(qb).read(buf);
        return new String(buf.array()).trim();
    }

    private void listenForQB() throws IOException {

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

        questionBanks = new HashMap<>();
        int MAX_CONNECTIONS = 2;

        while (questionBanks.size() < MAX_CONNECTIONS) {

            System.out.println("Waiting for select...");
            selector.select();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {

                SelectionKey ky = (SelectionKey) iter.next();

                if (ky.isAcceptable()) {

                    // Accept the new client connection
                    SocketChannel client = serverSocket.accept();
                    client.configureBlocking(false);

                    // Add the new connection to the selector
                    client.register(selector, SelectionKey.OP_READ);

                    if (questionBanks.get("Python") == null)
                        questionBanks.put("Python", client);
                    else
                        questionBanks.put("C", client);

                    System.out.println("Accepted new connection from client: " + client);
                }
                iter.remove();
            }

        }

        selector.close();
    }
}
