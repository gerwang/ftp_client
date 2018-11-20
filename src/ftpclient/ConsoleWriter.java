package ftpclient;

import java.io.IOException;

public interface ConsoleWriter {
    void write(String var) throws IOException;

    void flush() throws IOException;
}