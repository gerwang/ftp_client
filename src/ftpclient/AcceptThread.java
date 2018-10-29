package ftpclient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AcceptThread extends Thread {
    private ServerSocket listener;
    private Socket result;

    AcceptThread(ServerSocket listener) {
        this.listener = listener;
        result = null;
    }

    ServerSocket getListener() {
        return listener;
    }

    Socket getResult() {
        return result;
    }

    @Override
    public void run() {
        try {
            result = this.listener.accept();
            listener.close();
        } catch (IOException ignored) {
        }
    }
}
