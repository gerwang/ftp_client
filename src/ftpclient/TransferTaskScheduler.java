package ftpclient;

import ftpclient.exceptions.CommandFailException;
import ftpclient.exceptions.ConsoleCloseException;
import ftpclient.exceptions.SocketCloseException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TransferTaskScheduler extends Thread implements TransferListener {
    private JTable statusTable;
    private BlockingQueue<TransferTask> tasks;
    private FtpGui mainGui;
    private TransferTask activeTask = null;
    private long lastTime, accumulate;

    TransferTaskScheduler(JTable statusTable, FtpGui mainGui) {
        this.statusTable = statusTable;
        this.mainGui = mainGui;
        tasks = new LinkedBlockingQueue<>();
    }

    private static String getDirectionMark(String command) {
        switch (command) {
            case "RETR":
                return "<--";
            case "STOR":
                return "-->";
            default:
                return "???";
        }
    }

    void addTask(TransferTask task) {
        DefaultTableModel model = (DefaultTableModel) statusTable.getModel();
        model.addRow(new Object[]{
                task.fileName, "等待中", task.downloaded + "/" + task.total, task.localPath, getDirectionMark(task.command), task.remotePath, ""
        });
        task.row = model.getRowCount() - 1;
        model.fireTableRowsInserted(task.row, task.row);
        tasks.add(task);
    }

    public void run() {
        while (true) {
            TransferTask task;
            try {
                task = tasks.take();
            } catch (InterruptedException e) {
                return;
            }
            activeTask = task;
            statusTable.getModel().setValueAt("下载中", activeTask.row, 1);
            TransferThread transferThread = null;
            try {
                if (task.command.equals("RETR")) {
                    if (task.downloaded != 0) {
                        mainGui.getSession().handleRest("" + task.downloaded);
                    }
                    mainGui.getSession().handleGet(task.remotePath);
                    transferThread = mainGui.getSession().getRetrieveThread(task.localPath);
                } else if (task.command.equals("STOR")) {
                    mainGui.getSession().handlePut(task.localPath);
                    transferThread = mainGui.getSession().getStoreThread(task.remotePath);
                }
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException e) {
                onError(e.getMessage());
                continue;
            }


            assert transferThread != null;
            transferThread.setListener(this);

            lastTime = System.currentTimeMillis();
            accumulate = 0;
            transferThread.start();
            int ret;
            try {
                ret = mainGui.getSession().parseResponse();
            } catch (SocketCloseException | CommandFailException | ConsoleCloseException e) {
                onError(e.getMessage());
                continue;
            }
            try {
                transferThread.join();
            } catch (InterruptedException e) {
                return;
            }
            if (ret != 226) {
                onError("invalid response code: " + ret);
                continue;
            }
            activeTask = null;
        }
    }

    private void updateSpeed(long currentTime) {
        long elapsedTime = currentTime - lastTime;
        if (elapsedTime <= 0) {
            elapsedTime = 1;
        }
        lastTime = currentTime;
        String speedText = FTPConfig.humanReadableSize("" + accumulate * 1000 / elapsedTime) + "/s";
        accumulate = 0;
        statusTable.getModel().setValueAt(speedText, activeTask.row, 6);
    }

    @Override
    public void onProgress(int delta) {
        if (activeTask == null) {
            return;
        }
        activeTask.downloaded += delta;
        accumulate += delta;
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastTime + 1000) {
            updateSpeed(currentTime);
        }
        statusTable.getModel().setValueAt(activeTask.downloaded + "/" + activeTask.total, activeTask.row, 2);
    }

    @Override
    public void onError(String message) {
        JOptionPane.showMessageDialog(this.mainGui, message, "传输失败", JOptionPane.ERROR_MESSAGE);
        statusTable.getModel().setValueAt("已失败", activeTask.row, 1);
        mainGui.getSession().clearDataSocket();
        activeTask = null;
    }

    @Override
    public void onDone() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastTime && accumulate > 0) {
            updateSpeed(currentTime);
        }
        statusTable.getModel().setValueAt("已完成", activeTask.row, 1);
    }

    int getSize() {
        return this.tasks.size();
    }

    void clearTasks() {
        tasks.clear();
    }
}
