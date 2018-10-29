package ftpclient;

import ftpclient.exceptions.CommandFailException;
import ftpclient.exceptions.ConsoleCloseException;
import ftpclient.exceptions.SocketCloseException;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FTPSession {
    private Socket connectionSocket, dataSocket;
    private AcceptThread acceptThread;
    private InputStreamReader connectionReader;
    private OutputStreamWriter connectionWriter;
    private OutputStream consoleOut;

    private String host;
    private OutputStreamWriter consoleWriter;
    private String prompt;
    private SessionWait waitType;
    private SessionStatus status = SessionStatus.DISCONNECTED;
    private String previousPath = "/";

    String getPrompt() {
        return prompt;
    }

    private void setWaitType(SessionWait waitType) {
        switch (waitType) {
            case PORT:
                prompt = "port (default: 21): ";
                break;
            case COMMAND:
                prompt = "ftp> ";
                break;
            case PASSWORD:
                prompt = "password: ";
                break;
            case USERNAME:
                prompt = "username (default: " + System.getProperty("user.name") + "): ";
                break;
            case RNTO:
                prompt = "rename to (default: " + previousPath + "): ";
                break;
            case RETR:
                prompt = "save to local (default: " + previousPath + "): ";
                break;
            case STOR:
                prompt = "save to remote (default: " + previousPath + "): ";
                break;
        }
        this.waitType = waitType;
    }


    FTPSession(OutputStream out) {
        consoleWriter = new OutputStreamWriter(out);
        consoleOut = out;
        setWaitType(SessionWait.COMMAND);
    }

//    static boolean isNotMark(String line) {
//        if (line.length() < 4) {
//            return false;
//        }
//        if (!canCode(line)) {
//            return false;
//        }
//        return line.charAt(0) != '1';
//    }

    private static boolean canCode(String line) {
        return Character.isDigit(line.charAt(0)) &&
                Character.isDigit(line.charAt(1)) &&
                Character.isDigit(line.charAt(2)) &&
                line.charAt(3) == ' ';
    }

    private static int getCode(String line) {
        return (line.charAt(0) - '0') * 100
                + (line.charAt(1) - '0') * 10
                + (line.charAt(2) - '0');
    }

    private int parseResponse() throws SocketCloseException, CommandFailException, ConsoleCloseException {
        while (true) {
            if (connectionReader == null) {
                throw new CommandFailException("receive: connection not open");
            }
            byte[] buffer = new byte[4096];
            int index = 0;
            index = getIndex(buffer, index);
            if (index == buffer.length) {
                throw new SocketCloseException("line too long");
            } else {
                String line = new String(buffer, 0, index - 2);
                if (FTPConfig.logLevel >= LogLevel.INFO) {
                    try {
                        consoleWriter.write(line + '\n');
                        consoleWriter.flush(); // prev bug: ftl
                    } catch (IOException e) {
                        throw new ConsoleCloseException("console closed");
                    }
                }
                if (canCode(line)) {
                    return getCode(line);
                }
            }
        }
    }


    private String parseResponseLine() throws SocketCloseException, CommandFailException, ConsoleCloseException {
        while (true) {
            if (connectionReader == null) {
                throw new CommandFailException("receive: connection not open");
            }
            byte[] buffer = new byte[4096];
            int index = 0;
            index = getIndex(buffer, index);
            if (index == buffer.length) {
                throw new SocketCloseException("line too long");
            } else {
                String line = new String(buffer, 0, index - 2);
                if (FTPConfig.logLevel >= LogLevel.INFO) {
                    try {
                        consoleWriter.write(line + '\n');
                        consoleWriter.flush();
                    } catch (IOException e) {
                        throw new ConsoleCloseException(e.getMessage());
                    }
                }
                if (canCode(line)) {
                    return line;
                }
            }
        }
    }

    private int getIndex(byte[] buffer, int index) throws SocketCloseException {
        try {
            while (index < buffer.length) {
                buffer[index++] = (byte) connectionReader.read();
                if (index > 1 && buffer[index - 2] == '\r' && buffer[index - 1] == '\n') {
                    break;
                }
            }
        } catch (IOException e) {
            throw new SocketCloseException("connection closed");
        }
        return index;
    }

    private void doPort() throws CommandFailException, ConsoleCloseException, SocketCloseException {
        ServerSocket listener;
        try {
            listener = new ServerSocket(0); // prev bug: should use 0 to indicate any usable port
        } catch (IOException e) {
            throw new CommandFailException(e.getMessage());
        }
        byte[] address = connectionSocket.getLocalAddress().getAddress(); // prev bug: use proper local address
        if (address.length != 4) {
            throw new CommandFailException("ipv" + address.length + " address instead of ipv4 address");
        }
        int port = listener.getLocalPort();
        String msg = "PORT " + address[0] + "," + address[1] + "," + address[2]
                + "," + address[3] + "," + (port >> 8) + "," + (port & 255);
        writeConsole(msg);
        writeConnection(msg);
        acceptThread = new AcceptThread(listener);
        acceptThread.start();
        int ret = parseResponse();
        if (ret != 200) {
            throw new CommandFailException("cannot port accept: " + ret);
        }
    }

    private String ipRegex = "(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)";
    private Pattern ipPattern = Pattern.compile(ipRegex);

    private void doPassive() throws ConsoleCloseException, SocketCloseException, CommandFailException {
        String msg = "PASV";
        writeConsole(msg);
        writeConnection(msg);
        String line = parseResponseLine();
        int ret = getCode(line);
        if (ret != 227) {
            throw new CommandFailException("invalid response code: " + ret);
        }
        Matcher m = ipPattern.matcher(line);
        if (!m.find()) { // prev bug: matches
            throw new CommandFailException("PASV: regex do not match");
        } else {
            byte[] bytes = new byte[4];
            int port;
            try {
                for (int i = 0; i < 4; i++) {
                    bytes[i] = Byte.parseByte(m.group(i + 1));
                }
                port = (Integer.parseInt(m.group(5)) << 8) + Integer.parseInt(m.group(6));
            } catch (NumberFormatException e) {
                throw new CommandFailException(e.getMessage());
            }
            InetAddress address;
            try {
                address = InetAddress.getByAddress(bytes);
            } catch (UnknownHostException e) {
                throw new CommandFailException(e.getMessage());
            }
            try {
                dataSocket = new Socket(address, port);
            } catch (Exception e) {
                throw new CommandFailException(e.getMessage());
            }
        }
    }

    private void startTransfer() throws SocketCloseException, ConsoleCloseException, CommandFailException {
        if (FTPConfig.active) {// PORT
            doPort();
        } else {// PASV
            doPassive();
        }
    }

    private void clearDataSocket() {
        if (dataSocket != null) { // prev bug: forget to judge null
            try {
                dataSocket.close();
            } catch (IOException ignored) {
            }
            dataSocket = null;
        }
        if (acceptThread != null) {
            if (acceptThread.getListener() != null) {
                try {
                    acceptThread.getListener().close();
                } catch (IOException ignored) {
                }
            }
            try {
                acceptThread.join();
            } catch (InterruptedException ignored) {
            }
            acceptThread = null;
        }
    }

    private void processAfterMark() throws CommandFailException {
        if (FTPConfig.active) {
            try {
                acceptThread.join(FTPConfig.PortTimeout);
            } catch (InterruptedException ignored) {
            }
            if (acceptThread.getResult() != null) {
                dataSocket = acceptThread.getResult();
            } else {
                throw new CommandFailException("PORT timeout");
            }
        }
    }

    private void handleLs(String parameter) throws ConsoleCloseException, SocketCloseException, CommandFailException {
        startTransfer();
        String msg = "LIST " + parameter;
        writeConsole(msg);
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 150) {
            throw new CommandFailException("invalid response code: " + ret);
        }
        processAfterMark();
        TransferThread transferThread;
        try {
            transferThread = new TransferThread(dataSocket.getInputStream(), consoleOut, consoleWriter);
        } catch (IOException e) {
            throw new CommandFailException(e.getMessage());
        }

        transferThread.start();
        ret = parseResponse();
        try {
            transferThread.join();
        } catch (InterruptedException ignored) {
        }
        if (ret != 226) {
            throw new CommandFailException("invalid response code: " + ret);
        }
        clearDataSocket();
    }

    private void handleExternalCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command, null, FTPConfig.cwd.toFile()); // prev bug: forget cwd
            BufferedReader externalReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                String line;
                line = externalReader.readLine();
                if (line == null) {
                    break;
                }
                consoleWriter.write(line + '\n');
                consoleWriter.flush();
            }
        } catch (IOException e) {
            if (FTPConfig.logLevel >= LogLevel.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private void handleLocalChangeDir(String command) throws CommandFailException {
        Path newCwd = FTPConfig.cwd.resolve(command.trim()).normalize();
        if (!Files.exists(newCwd)) {
            throw new CommandFailException("directory does not exist: " + newCwd);
        }
        FTPConfig.cwd = newCwd;
    }

    private void handleClose() throws ConsoleCloseException, SocketCloseException, CommandFailException {
        String msg = "QUIT";
        writeConsole(msg);
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 221) {
            throw new CommandFailException("invalid response code: " + ret);
        }
        resetSocket(); // prev bug: ftl
    }

    private void handleQuit() {
        if (status == SessionStatus.DISCONNECTED) {
            System.exit(0);
        }
        try {
            handleClose();
            System.exit(0);
        } catch (Exception e) {
            if (FTPConfig.logLevel >= LogLevel.WARN) {
                try {
                    consoleWriter.write(e.getMessage());
                    consoleWriter.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            System.exit(1);
        }
    }

    private void handleSystem() throws ConsoleCloseException, SocketCloseException, CommandFailException {
        String msg = "SYST";
        writeConsole(msg);
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 215) {
            throw new CommandFailException("invalid response code: " + ret);
        }
    }

    private void handlePass() throws ConsoleCloseException {
        FTPConfig.active = !FTPConfig.active;
        if (FTPConfig.logLevel > LogLevel.NONE) {
            try {
                consoleWriter.write("current mode: " + (FTPConfig.active ? "active" : "passive") + "\n");
                consoleWriter.flush();
            } catch (IOException e) {
                throw new ConsoleCloseException("");
            }
        }
    }

    private void handleUser(String username) throws ConsoleCloseException, SocketCloseException, CommandFailException {
        if (username.trim().equals("")) {
            username = "" + System.getProperty("user.name");
        }
        String msg = "USER " + username.trim();
        writeConsole(msg);
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 331) {
            throw new CommandFailException("invalid response code: " + ret);
        }
        setWaitType(SessionWait.PASSWORD); // prev bug ftl
    }

    private void handleBinary() throws ConsoleCloseException, SocketCloseException, CommandFailException {
        String msg = "TYPE I";
        writeConsole(msg);
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 200) {
            throw new CommandFailException("invalid response code: " + ret);
        }
    }

    private void handleCd(String parameter) throws ConsoleCloseException, SocketCloseException, CommandFailException {
        String msg = "CWD " + parameter;
        writeConsole(msg);
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 200 && ret != 250) {
            throw new CommandFailException("invalid response code: " + ret);
        }
    }

    private void handleMkdir(String parameter) throws ConsoleCloseException, SocketCloseException, CommandFailException {
        String msg = "MKD " + parameter;
        writeConsole(msg);
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 257 && ret != 250) {
            throw new CommandFailException("invalid response code: " + ret);
        }
    }

    private void handleRmdir(String parameter) throws ConsoleCloseException, SocketCloseException, CommandFailException {
        String msg = "RMD " + parameter;
        writeConsole(msg);
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 250) {
            throw new CommandFailException("invalid response code: " + ret);
        }
    }


    private Pattern doubleQuote = Pattern.compile("\"([\\s\\S]+)\"");

    private String doPwd(String parameter) throws ConsoleCloseException, SocketCloseException, CommandFailException {
        String msg = "PWD " + parameter;
        writeConsole(msg);
        writeConnection(msg);
        String line = parseResponseLine();
        int ret = getCode(line);
        if (ret != 257) {
            throw new CommandFailException("invalid response code: " + ret);
        }
        Matcher m = doubleQuote.matcher(line);
        if (!m.find()) {
            throw new CommandFailException("cannot parse pwd response");
        } else {
            return m.group(1);
        }
    }

    private void handlePwd(String parameter) throws CommandFailException, ConsoleCloseException, SocketCloseException {
        String res = doPwd(parameter);
        try {
            consoleWriter.write(res + "\n"); // prev bug: forget new line
            consoleWriter.flush();
        } catch (IOException e) {
            throw new ConsoleCloseException(e.getMessage());
        }
    }

    private void handleRename(String parameter) throws ConsoleCloseException, SocketCloseException, CommandFailException {
        String msg = "RNFR " + parameter;
        writeConsole(msg);
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 350) {
            throw new CommandFailException("invalid response code: " + ret);
        }
        previousPath = parameter;
        setWaitType(SessionWait.RNTO);
    }

    private void provideRenameTo(String parameter) throws ConsoleCloseException, SocketCloseException, CommandFailException {
        setWaitType(SessionWait.COMMAND);

        if (parameter.trim().equals("")) {
            parameter = previousPath;
        }
        String msg = "RNTO " + parameter;
        writeConsole(msg);
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 250) {
            throw new CommandFailException("invalid response code: " + ret);
        }
    }

    private void writeConsole(String msg) throws ConsoleCloseException {
        if (FTPConfig.logLevel >= LogLevel.DEBUG) {
            try {
                consoleWriter.write("--> " + msg + "\n");
                consoleWriter.flush();
            } catch (IOException e) {
                throw new ConsoleCloseException(e.getMessage());
            }
        }
    }

    private void writeConnection(String msg) throws SocketCloseException {
        try {
            connectionWriter.write(msg.trim() + "\r\n"); // prev bug: no trim()
            connectionWriter.flush();
        } catch (IOException e) {
            throw new SocketCloseException(e.getMessage());
        }
    }

    private void providePassword(String password) throws ConsoleCloseException, SocketCloseException, CommandFailException {
        setWaitType(SessionWait.COMMAND);

        String msg = "PASS " + password;
        writeConsole("PASS XXXX");
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 230) {
            throw new CommandFailException("invalid response code: " + ret);
        }
        status = SessionStatus.LOGGEDIN;
    }

    private void handlePut(String parameter) {
        previousPath = parameter;
        setWaitType(SessionWait.STOR);
    }

    private void provideStore(String parameter) throws CommandFailException, ConsoleCloseException, SocketCloseException {
        setWaitType(SessionWait.COMMAND);
        if (parameter.trim().equals("")) {
            parameter = previousPath;
        }
        File file = FTPConfig.cwd.resolve(previousPath).toFile();
        FileInputStream in;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new CommandFailException(e.getMessage());
        }
        startTransfer();
        String msg = "STOR " + parameter;
        writeConsole(msg);
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 150) {
            throw new CommandFailException("invalid code response: " + ret);
        }
        processAfterMark();
        TransferThread transferThread;
        try {
            transferThread = new TransferThread(in, dataSocket.getOutputStream(), consoleWriter);
        } catch (IOException e) {
            throw new CommandFailException(e.getMessage());
        }

        transferThread.start();
        ret = parseResponse();
        try {
            transferThread.join();
        } catch (InterruptedException ignored) {
        }
        if (ret != 226) {
            throw new CommandFailException("invalid response code: " + ret);
        }
        try {
            in.close();
        } catch (IOException ignored) {
        }
        clearDataSocket();
    }

    private void handleGet(String parameter) {
        previousPath = parameter;
        setWaitType(SessionWait.RETR);
    }

    private void provideRetrieve(String parameter) throws CommandFailException, ConsoleCloseException, SocketCloseException {
        setWaitType(SessionWait.COMMAND);
        if (parameter.trim().equals("")) {
            parameter = previousPath;
        }
        File file = FTPConfig.cwd.resolve(parameter).toFile();
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new CommandFailException(e.getMessage());
        }
        startTransfer();
        String msg = "RETR " + previousPath;
        writeConsole(msg);
        writeConnection(msg);
        int ret = parseResponse();
        if (ret != 150) {
            throw new CommandFailException("invalid code response: " + ret);
        }
        processAfterMark();
        TransferThread transferThread;
        try {
            transferThread = new TransferThread(dataSocket.getInputStream(), out, consoleWriter);
        } catch (IOException e) {
            throw new CommandFailException(e.getMessage());
        }

        transferThread.start();
        ret = parseResponse();
        try {
            transferThread.join();
        } catch (InterruptedException ignored) {
        }
        if (ret != 226) {
            throw new CommandFailException("invalid response code: " + ret);
        }
        try {
            out.close();
        } catch (IOException ignored) {
        }
        clearDataSocket();
    }

    private void provideUsername(String username) throws CommandFailException, ConsoleCloseException, SocketCloseException {
        setWaitType(SessionWait.COMMAND);

        handleUser(username);
    }

    private void handleOpen(String parameter) {
        host = parameter.trim();
        setWaitType(SessionWait.PORT);
    }

    private void providePort(String portName) throws SocketCloseException, ConsoleCloseException, CommandFailException {
        setWaitType(SessionWait.COMMAND);
        int port;
        if (portName.trim().equals("")) {
            port = 21;
        } else {
            try {
                port = Integer.parseInt(portName);
            } catch (NumberFormatException e) {
                if (FTPConfig.logLevel >= LogLevel.DEBUG) {
                    e.printStackTrace();
                }
                try {
                    consoleWriter.write("bad port -- " + portName + "\n");
                    consoleWriter.flush();
                } catch (IOException e1) {
                    if (FTPConfig.logLevel >= LogLevel.DEBUG) {
                        e1.printStackTrace();
                    }
                    throw new ConsoleCloseException("console closed");
                }
                throw new CommandFailException("port cannot be parsed to int");
            }
        }
        try {
            connectionSocket = new Socket(host, port);
            connectionReader = new InputStreamReader(connectionSocket.getInputStream());
            connectionWriter = new OutputStreamWriter(connectionSocket.getOutputStream());
        } catch (IOException e) {
            if (FTPConfig.logLevel >= LogLevel.DEBUG) {
                e.printStackTrace();
            }
            throw new SocketCloseException("initialize socket fail");
        }
        int ret = parseResponse();
        if (ret != 220) { // prev bug: / to %
            throw new SocketCloseException("server invalid response: " + ret);
        }
        status = SessionStatus.CONNECTED;
    }

    private void handleHelp() throws ConsoleCloseException {
        int numberPerLine = 5;
        try {
            for (int i = 0; i < FTPConfig.supportedCommands.length; i++) {
                consoleWriter.write(FTPConfig.supportedCommands[i] + ((i + 1) % numberPerLine == 0 ? "\n" : " "));
            }
            if (FTPConfig.supportedCommands.length % numberPerLine != 0) {
                consoleWriter.write("\n");
            }
            consoleWriter.flush();
        } catch (IOException e) {
            throw new ConsoleCloseException(e.getMessage());
        }
    }

    private void handleDebug() throws ConsoleCloseException {
        toggleDebugLevel(LogLevel.DEBUG, "debug mode on", "debug mode off");
    }

    private void toggleDebugLevel(int debug, String s, String s2) throws ConsoleCloseException {
        try {
            if (FTPConfig.logLevel != debug) {
                FTPConfig.logLevel = debug;
                consoleWriter.write(s + "\n");
                consoleWriter.flush();
            } else {
                FTPConfig.logLevel = LogLevel.WARN;
                consoleWriter.write(s2 + "\n");
                consoleWriter.flush();
            }
        } catch (IOException e) {
            throw new ConsoleCloseException(e.getMessage());
        }
    }

    private void handleVerbose() throws ConsoleCloseException {
        toggleDebugLevel(LogLevel.INFO, "verbose mode on", "verbose mode off");
    }

    private void parseCommandLine(String commandLine) throws ConsoleCloseException, CommandFailException, SocketCloseException {
        if (commandLine.trim().startsWith("!")) {
            handleExternalCommand(commandLine.trim().substring(1)); // prev bug: no trim()
        } else {
            int index = 0;
            while (index < commandLine.length() && Character.isWhitespace(commandLine.charAt(index))) {
                index++;
            }
            int end_index = index;
            while (end_index < commandLine.length() && !Character.isWhitespace(commandLine.charAt(end_index))) {
                end_index++;
            }
            String command = commandLine.substring(index, end_index);
            String parameter = commandLine.substring(Math.min(end_index + 1, commandLine.length())); // skip the space prev bug: forget min
            switch (command) {
                case "lcd":
                    handleLocalChangeDir(parameter);
                    return;
                case "open":
                    handleOpen(parameter);
                    return;
                case "pass":
                    handlePass();
                    return;
                case "bye":
                case "quit":
                    handleQuit();
                    return;
                case "help":
                    handleHelp();
                    return;
                case "debug":
                    handleDebug();
                    return;
                case "verbose":
                    handleVerbose();
                    return;
            }
            if (status == SessionStatus.DISCONNECTED) {
                throw new CommandFailException("invalid command, maybe connect first?");
            }
            switch (command) {
                case "binary":
                    handleBinary();
                    return;
                case "system":
                    handleSystem();
                    return;
                case "close":
                    handleClose();
                    return;
                case "user":
                    handleUser(parameter);
                    return;
                case "ls":
                case "dir":
                    handleLs(parameter);
                    return;
                case "rename":
                    handleRename(parameter);
                    return;
                case "get":
                case "recv":
                    handleGet(parameter);
                    return;
                case "put":
                case "send":
                    handlePut(parameter);
                    return;
                case "cd":
                    handleCd(parameter);
                    return;
                case "mkdir":
                    handleMkdir(parameter);
                    return;
                case "rmdir":
                    handleRmdir(parameter);
                    return;
                case "pwd":
                    handlePwd(parameter);
                    return;
            }
            throw new CommandFailException("invalid command, type help to see valid commands");
        }
    }

    void provideString(String command) throws CommandFailException, ConsoleCloseException, SocketCloseException {
        switch (waitType) {
            case COMMAND:
                parseCommandLine(command);
                break;
            case PORT:
                providePort(command);
                break;
            case PASSWORD:
                providePassword(command);
                break;
            case RNTO:
                provideRenameTo(command);
                break;
            case RETR:
                provideRetrieve(command);
                break;
            case STOR:
                provideStore(command);
                break;
            case USERNAME:
                provideUsername(command);
        }
    }

    void startUp() throws CommandFailException, ConsoleCloseException, SocketCloseException {
        if (!FTPConfig.host.equals("")) {
            handleOpen(FTPConfig.host);
            providePort("" + FTPConfig.port);
            setWaitType(SessionWait.USERNAME);
        }
    }

    void resetSocket() {
        if (connectionReader != null) {
            try {
                connectionReader.close();
            } catch (IOException ignored) {
            }
            connectionReader = null;
        }
        if (connectionWriter != null) {
            try {
                connectionWriter.close();
            } catch (IOException ignored) {
            }
            connectionWriter = null;
        }
        if (connectionSocket != null) {
            try {
                connectionSocket.close();
            } catch (IOException ignored) {
            }
            connectionSocket = null;
        }
        clearDataSocket();
        status = SessionStatus.DISCONNECTED;
    }
}
