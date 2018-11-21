package ftpclient;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleEmulator implements ConsoleWriter {
    private HTMLEditorKit htmlEditorKit;
    private HTMLDocument document;

    private static String ipRegex = "(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)";
    private static Pattern codeMatcher = Pattern.compile("^\\d\\d\\d .*$", Pattern.DOTALL);
    private static String[] codeColors = new String[]{
            null, "#ADFF2F", "#7CFC0", "#228B22", "#FF6347", "#B22222"
    };
    private static String[] commands = new String[]
            {"USER", "PASS", "RETR", "STOR", "QUIT", "SYST", "TYPE", "PORT", "PASV", "MKD", "CWD", "PWD", "LIST", "RMD", "RNFR", "RNTO", "REST"};

    ConsoleEmulator(JTextPane textPane) {
        htmlEditorKit = new HTMLEditorKit();
        document = new HTMLDocument();
        textPane.setEditorKit(htmlEditorKit);
        textPane.setDocument(document);
    }

    private static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    @Override
    public void write(String var) {
        var = escapeHTML(var);
        if (codeMatcher.matcher(var).matches()) {
            String head = var.substring(0, 3);
            String body = var.substring(3);
            int index = Integer.parseInt(head.substring(0, 1));
            if (index > 0 && index < codeColors.length) {
                var = "<font color=\"" + codeColors[index] + "\">" + head + "</font>" + body;
            }
        }
        for (String command : commands) {
            var = var.replaceAll(command, "<font color=\"#8A2BE2\"><b>" + command + "</b></font>");
        }
        var = var.replaceAll(ipRegex, "<b>$0</b>");
        System.out.println(var);
        try {
            htmlEditorKit.insertHTML(document, document.getLength(), "<p>" + var + "</p>", 0, 0, null);
        } catch (BadLocationException | IOException ignored) {
        }
    }

    @Override
    public void flush() {
    }
}
