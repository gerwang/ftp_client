package ftpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class TransferThread extends Thread {
    private InputStream in;
    private OutputStream out;
    private OutputStreamWriter console;
    private byte[] buffer;

    TransferThread(InputStream in, OutputStream out, OutputStreamWriter console) {
        this.in = in;
        this.out = out;
        this.console = console;
        buffer = new byte[4096];
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
            } catch (IOException e) {
                if (FTPConfig.logLevel >= LogLevel.DEBUG) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        try {
            out.flush();
        } catch (IOException ignored) {
        }
        if (FTPConfig.logLevel >= LogLevel.INFO) {
            try {
                console.write("transfer complete: " + count + " bytes\n");
                console.flush();
            } catch (IOException ignored) {
            }
        }
    }
}
