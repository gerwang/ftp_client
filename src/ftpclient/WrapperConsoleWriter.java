package ftpclient;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class WrapperConsoleWriter implements ConsoleWriter {
    private OutputStreamWriter writer;

    WrapperConsoleWriter(OutputStream out) {
        writer = new OutputStreamWriter(out);
    }

    @Override
    public void write(String var) throws IOException {
        writer.write(var);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }
}
