package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManagementService implements Runnable {

    private final SSLServerSocket serverSocket;
    private final ExecutorService pool;
    private ServerState serverState = ServerState.getInstance();

    public ManagementService(int port, int poolSize) throws IOException {
        SSLServerSocketFactory sslserversocketfactory =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        serverSocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(port);
        pool = Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public void run() {
        try {

            logger.info("Server listening peer on management port "+ serverSocket.getLocalPort() +" for a connection...");

            while (!serverState.isStopRunning()) {
                pool.execute(new ManagementConnection((SSLSocket) serverSocket.accept()));
            }

            pool.shutdown();
        } catch (IOException ex) {
            pool.shutdown();
        } finally {
            pool.shutdownNow();
        }
    }

    private static final Logger logger = LogManager.getLogger(ManagementService.class);
}
