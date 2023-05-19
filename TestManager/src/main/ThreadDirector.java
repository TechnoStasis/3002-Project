package main;

public class ThreadDirector {
	private boolean isBlocking = false;

    public void startMonitoring(ProtocolMethods server) {
        Thread monitoringThread = new Thread(() -> {
            while (true) {
                if (server.isBlocking()) {
                    setBlocking(true);
                } else {
                    setBlocking(false);
                }
            }
        });

        monitoringThread.start();
    }

    public synchronized boolean isBlocking() {
        return isBlocking;
    }

    private synchronized void setBlocking(boolean blocking) {
        isBlocking = blocking;
    }
}
