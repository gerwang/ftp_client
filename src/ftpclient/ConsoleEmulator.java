package ftpclient;

import javax.swing.*;
import java.io.IOException;

public class ConsoleEmulator implements ConsoleWriter {
    private JTextPane textPane;

    ConsoleEmulator(JTextPane textPane) {
        this.textPane = textPane;
    }

    @Override
    public void write(String var) {
        textPane.setText(textPane.getText() + var); // TODO
    }

    @Override
    public void flush() {
    }
}
