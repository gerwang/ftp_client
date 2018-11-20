package ftpclient;

import javax.swing.*;
import javax.swing.plaf.FileChooserUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.*;
import java.text.DecimalFormat;

public class LsOutputStream extends OutputStream {
    private StringBuilder builder;
    private JTable fileTable;

    LsOutputStream(JTable fileTable) {
        this.fileTable = fileTable;
        builder = new StringBuilder();
    }

    @Override
    public void write(int i) throws IOException {
        builder.append((char) i);
    }


    @Override
    public void flush() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(builder.toString().getBytes())));
        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
        model.setNumRows(0);
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            String[] fileToks = line.split("\\s+", 9);
            if (fileToks.length < 9)
                continue;
            String filePermissions = fileToks[0];
//            String numHardLinks = fileToks[1];
            String fileOwner = fileToks[2];
            String fileOwnerGroup = fileToks[3];
            String fileSize = fileToks[4];
            String lastModDate = fileToks[5] + " " + fileToks[6] + " " + fileToks[7];
            String fileName = fileToks[8];
            long size = 0;
            try {
                size = Long.parseLong(fileSize);
            } catch (NumberFormatException ignored) {
            }

            model.addRow(new Object[]{
                    fileName, FTPConfig.humanReadableSize(fileSize), lastModDate, filePermissions, fileOwner, fileOwnerGroup, size
            });
        }
        model.fireTableDataChanged();
        builder = new StringBuilder();
    }
}
