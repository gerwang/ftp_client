package ftpclient;

import org.apache.commons.cli.*;

public class Main {

    private static void parseArguments(String[] args) {
        Options ops = new Options();
        Option active = new Option("a", "active", false, "enable active mode transfer");
        ops.addOption(active);
        Option gui = new Option("g", "gui", false, "enable gui (default is off)");
        ops.addOption(gui);
        Option debug = new Option("d", "debug", false, "enable debugging output");
        ops.addOption(debug);
        Option verbose = new Option("v", "verbose", false, "verbose output");
        ops.addOption(verbose);
        Option help = new Option("h", "help", false, "give this help list");
        ops.addOption(help);
        Option version = new Option("V", "version", false, "print program version");
        ops.addOption(version);
        Option cwd = new Option("c", "cwd", true, "set current working directory");
        ops.addOption(cwd);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(ops, args);
            if (cmd.getArgs().length > 2) {
                throw new ParseException("wrong positional argument length " + cmd.getArgs().length);
            } else if (cmd.getArgs().length == 2) {
                FTPConfig.host = cmd.getArgs()[0];
                try {
                    FTPConfig.port = Integer.parseInt(cmd.getArgs()[1]);
                } catch (NumberFormatException e) {
                    throw new ParseException("port cannot be converted to int: " + cmd.getArgs()[1]);
                }
            } else if (cmd.getArgs().length == 1) {
                FTPConfig.host = cmd.getArgs()[0];
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("client [options] [host [port]]", ops);
            System.exit(1);
        }

        if (cmd.hasOption('a')) {
            FTPConfig.active = true;
        }
        if (cmd.hasOption('g')) {
            FTPConfig.gui = true;
        }
        if (cmd.hasOption('v')) {
            FTPConfig.logLevel = LogLevel.INFO;
        }
        if (cmd.hasOption('d')) {
            FTPConfig.logLevel = LogLevel.DEBUG;
        }
        if (cmd.hasOption('h')) {
            formatter.printHelp("client [options] [host [port]]", ops);
            System.exit(0);
        }
        if (cmd.hasOption('V')) {
            System.out.println(FTPConfig.version);
            System.exit(0);
        }
        if (cmd.hasOption('c')) {
            FTPConfig.cwdArg = cmd.getOptionValue('c'); //<@bug not used
        }
    }

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        parseArguments(args);
        if (!FTPConfig.gui) {
            FTPCommandLine commandLine = new FTPCommandLine();
            commandLine.mainLoop();
        } else {
            FtpGui.main(args);
        }
    }
}
