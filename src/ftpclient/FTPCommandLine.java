package ftpclient;


import ftpclient.exceptions.CommandFailException;
import ftpclient.exceptions.ConsoleCloseException;
import ftpclient.exceptions.SocketCloseException;
import org.jline.builtins.Completers.FileNameCompleter;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

class FTPCommandLine {
    private FTPSession session;

    FTPCommandLine() {
        session = new FTPSession(System.out);
    }

    void mainLoop() {
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            LineReader reader = LineReaderBuilder.builder().completer(new StringsCompleter(FTPConfig.supportedCommands))
                    .terminal(terminal).build();

            boolean first = true;
            while (true) {
                String line;
                try {
                    if (first) {
                        first = false;
                        session.startUp();
                    } else {
                        line = reader.readLine(session.getPrompt());
                        session.provideString(line);
                    }
                } catch (CommandFailException e) {
                    if (FTPConfig.logLevel >= LogLevel.WARN) {
                        System.out.println("command failed: " + e.getMessage());
                    }
                } catch (ConsoleCloseException e) {
                    if (FTPConfig.logLevel >= LogLevel.DEBUG) {
                        System.out.println("console closed: " + e.getMessage());
                        session.resetSocket();
                        break;
                    }
                } catch (SocketCloseException e) {
                    if (FTPConfig.logLevel >= LogLevel.WARN) {
                        System.out.println("connection closed: " + e.getMessage());
                    }
                    session.resetSocket();
                } catch (UserInterruptException | EndOfFileException e) {
                    session.resetSocket();
                    break;
                }
            }
        } catch (IOException e) {
            if (FTPConfig.logLevel >= LogLevel.ERROR) {
                e.printStackTrace();
            }
        }
    }
}
