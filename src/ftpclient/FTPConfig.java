package ftpclient;

import java.nio.file.Path;
import java.nio.file.Paths;

class FTPConfig {
    static int logLevel = LogLevel.WARN;
    static Path cwd = Paths.get(System.getProperty("user.dir"));
    static final String version = "0.1.0";
    static int port = 21;
    static boolean active = false;
    static boolean gui = false;
    static String cwdArg = "";
    static String host = "";
    static long PortTimeout = 1000;
    static String[] supportedCommands = {
            "lcd", "open", "pass", "bye", "quit",
            "help", "debug", "verbose", "binary", "system",
            "close", "user", "ls", "dir", "rename",
            "get", "recv", "put", "send", "cd",
            "mkdir", "rmdir", "pwd"};
}
