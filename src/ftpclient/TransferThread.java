package ftpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class TransferThread extends Thread {
    private InputStream in;
    private OutputStream out;
    private ConsoleWriter console;
    private byte[] buffer;
    private boolean closeOut;
    private boolean closeIn;
    private FTPSession session;
    private TransferListener listener = null;

    TransferThread(InputStream in, OutputStream out, ConsoleWriter console, boolean closeOut, FTPSession session, boolean closeIn) {
        this.in = in;
        this.out = out;
        this.console = console;
        buffer = new byte[4096];
        this.closeOut = closeOut;
        this.session = session;
        this.closeIn = closeIn;
    }

    void setListener(TransferListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        int count = 0;
        while (true) {
            try {
                int ret = in.read(buffer);
                if (ret == -1) {
                    break;
                }
                out.write(buffer, 0, ret);
                count += ret;
                if (listener != null) {
                    listener.onProgress(ret);
                }
            } catch (IOException e) {
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
                if (FTPConfig.logLevel >= LogLevel.DEBUG) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        try {
            out.flush();
            if (closeOut) {
                out.close();
            }
        } catch (IOException ignored) {
        }
        if (closeIn) {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
        if (FTPConfig.logLevel >= LogLevel.INFO) {
            try {
                console.write("transfer complete: " + count + " bytes\n");
                console.flush();
            } catch (IOException ignored) {
            }
        }

        session.clearDataSocket();
        if (listener != null) {
            listener.onDone();
        }
    }
}
