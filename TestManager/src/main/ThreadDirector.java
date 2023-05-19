package main;

/**
 * @authors 22887893 YVES MIGUEL REYES 33.3%
 * @authors 23262446 SRINIKETH KARLAPUDI 33.3%
 * @authors 23468614 CHENG LI 33.3%
 */
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
