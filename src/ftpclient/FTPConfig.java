package ftpclient;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

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
            "mkdir", "rmdir", "pwd", "!"};

    static boolean isDirectory(String filePermissions) {
        if (filePermissions.length() == 0) {
            return false;
        }
        return filePermissions.charAt(0) == 'd';
    }

    static String humanReadableSize(String sizeString) {
        DecimalFormat form = new DecimalFormat("0.00");
        String[] suffixs = new String[]{"Bytes", "kB", "MB", "GB", "TB"};
        float size = 0;
        try {
            size = Float.parseFloat(sizeString);
        } catch (NumberFormatException e) {
            return sizeString;
        }
        for (String suffix : suffixs) {
            if (size < 1024) {
                return form.format(size) + " " + suffix;
            }
            size /= 1024;
        }
        return size + " PB";
    }

    static ImageIcon getIconByFileType(String filePermissions) {
        String fileName = isDirectory(filePermissions) ? "directory.png" : "file.png";
        Image image = Toolkit.getDefaultToolkit().getImage("/res/" + fileName);
        return new ImageIcon(image);
    }
}
