/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpclient;

import ftpclient.exceptions.CommandFailException;
import ftpclient.exceptions.ConsoleCloseException;
import ftpclient.exceptions.SocketCloseException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @author gerw
 */
public class FtpGui extends javax.swing.JFrame implements PwdListener {
    private FTPSession session;
    private ArrayList<String> addressStack;
    private int addressTop;
    private TransferTaskScheduler scheduler;
    private String cwd = "";

    /**
     * Creates new form FtpGui
     */
    public FtpGui() {
        initComponents();
        LsOutputStream lsOut = new LsOutputStream(fileTable);
        ConsoleWriter consoleEmulator = new ConsoleEmulator(consoleText);
        addressStack = new ArrayList<>();
        session = new FTPSession(consoleEmulator, lsOut);
        session.setPwdListener(this);
        addressTop = 0;
        scheduler = new TransferTaskScheduler(statusTable, this);
        scheduler.start();

        ipText.setText("127.0.0.1");
        portText.setText("21");
        usernameText.setText("anonymous");
        passwordText.setText("anonymous");

        consoleText.setEditable(false);

        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    try {
                        int row = fileTable.rowAtPoint(mouseEvent.getPoint());
                        if (row == -1) {
                            return;
                        }
                        int modelRow = fileTable.convertRowIndexToModel(row);
                        String fileName = (String) fileTable.getModel().getValueAt(modelRow, 0);
                        String filePermissions = (String) fileTable.getModel().getValueAt(modelRow, 3);
                        if (!FTPConfig.isDirectory(filePermissions)) {
                            fileTable.clearSelection();
                            fileTable.addRowSelectionInterval(row, row);
                            doDownload();
                        } else {
                            routeTo(Paths.get(cwd).resolve(fileName).toString());
                        }
                    } catch (SocketCloseException | ConsoleCloseException e) {
                        JOptionPane.showMessageDialog(FtpGui.this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
                        try {
                            disconnect();
                        } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
                        }
                    } catch (CommandFailException e) {
                        JOptionPane.showMessageDialog(FtpGui.this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        statusTable.getColumn("进度").setCellRenderer((jTable, o, b, b1, i, i1) -> {
            double progress = (double) o;
            JProgressBar progressBar = new JProgressBar();
            progressBar.setValue((int) (progress * 100));
            return progressBar;
        });

        downloadButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/download.gif")), downloadButton)));
        uploadButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/upload.gif")), uploadButton)));
        backwardButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/back.gif")), backwardButton)));
        forwardButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/forward.gif")), forwardButton)));
        removeButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/delete.gif")), removeButton)));
        mkdirButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/mkd.gif")), mkdirButton)));
        connectButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/connect.gif")), connectButton)));
        disconnectButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/disconnect.gif")), disconnectButton)));
        portPasvButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/port.gif")), portPasvButton)));
        gotoButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/goto.gif")), gotoButton)));
        cdUpButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/goback.gif")), cdUpButton)));
        renameButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/rename.gif")), renameButton)));
        refreshButton.setIcon(new ImageIcon(resizeImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/refresh.gif")), refreshButton)));

    }

    private static Image resizeImage(Image img, JButton jb) {
        int offset = jb.getInsets().left;
        int resizedHeight = jb.getHeight() - offset;
        return img.getScaledInstance(resizedHeight, resizedHeight, Image.SCALE_SMOOTH);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        fileTable = new javax.swing.JTable();
        ipText = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        portText = new javax.swing.JTextField();
        usernameText = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        passwordText = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        addressText = new javax.swing.JTextField();
        connectButton = new javax.swing.JButton();
        disconnectButton = new javax.swing.JButton();
        portPasvButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        statusTable = new javax.swing.JTable();
        cdUpButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        gotoButton = new javax.swing.JButton();
        forwardButton = new javax.swing.JButton();
        backwardButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        consoleText = new javax.swing.JTextPane();
        mkdirText = new javax.swing.JTextField();
        mkdirButton = new javax.swing.JButton();
        uploadButton = new javax.swing.JButton();
        renameButton = new javax.swing.JButton();
        downloadButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("client");

        fileTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "名称", "大小", "修改时间", "属性", "所有者", "用户组", "实际大小"
                }
        ) {
            boolean[] canEdit = new boolean[]{
                    false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        fileTable.setShowHorizontalLines(false);
        jScrollPane2.setViewportView(fileTable);

        jLabel1.setText("服务器地址");

        jLabel2.setText("端口号");

        jLabel3.setText("用户名");

        jLabel4.setText("密码");

        connectButton.setText("连接");
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });

        disconnectButton.setText("断开");
        disconnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectButtonActionPerformed(evt);
            }
        });

        portPasvButton.setText("切换PORT和PASV");
        portPasvButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portPasvButtonActionPerformed(evt);
            }
        });

        statusTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "名称", "状态", "进度", "本地路径", "<->", "远程路径", "速度"
                }
        ) {
            boolean[] canEdit = new boolean[]{
                    false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        statusTable.setShowHorizontalLines(false);
        jScrollPane1.setViewportView(statusTable);

        cdUpButton.setText("返回");
        cdUpButton.setToolTipText("回到上一级目录");
        cdUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cdUpButtonActionPerformed(evt);
            }
        });

        refreshButton.setText("刷新");
        refreshButton.setToolTipText("刷新页面");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        gotoButton.setText("前往");
        gotoButton.setToolTipText("前往");
        gotoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoButtonActionPerformed(evt);
            }
        });

        forwardButton.setText("前进");
        forwardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forwardButtonActionPerformed(evt);
            }
        });

        backwardButton.setText("后退");
        backwardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backwardButtonActionPerformed(evt);
            }
        });

        jScrollPane4.setViewportView(consoleText);

        mkdirButton.setText("创建新目录");
        mkdirButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mkdirButtonActionPerformed(evt);
            }
        });

        uploadButton.setText("上传文件");
        uploadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadButtonActionPerformed(evt);
            }
        });

        renameButton.setText("重命名选中文件夹");
        renameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameButtonActionPerformed(evt);
            }
        });

        downloadButton.setText("下载选中文件");
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        removeButton.setText("删除选中文件夹");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("可以双击文件或文件夹");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(mkdirText, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(mkdirButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(uploadButton)
                                                .addGap(12, 12, 12)
                                                .addComponent(downloadButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(renameButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(removeButton)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jScrollPane4)
                                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jLabel1)
                                                                        .addComponent(backwardButton))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(ipText, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(jLabel2)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(portText, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(jLabel3)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(usernameText, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(jLabel4)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(passwordText, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                .addComponent(connectButton)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                .addComponent(disconnectButton))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(forwardButton)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                .addComponent(portPasvButton)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(jLabel5)
                                                                                .addGap(0, 0, Short.MAX_VALUE))))
                                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                                .addComponent(addressText)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(gotoButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(cdUpButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(refreshButton)))
                                                .addContainerGap())))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(ipText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2)
                                        .addComponent(portText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3)
                                        .addComponent(usernameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel4)
                                        .addComponent(passwordText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(disconnectButton)
                                        .addComponent(connectButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(forwardButton)
                                        .addComponent(backwardButton)
                                        .addComponent(portPasvButton)
                                        .addComponent(jLabel5))
                                .addGap(5, 5, 5)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(addressText)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(gotoButton)
                                                .addComponent(cdUpButton)
                                                .addComponent(refreshButton)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(mkdirText, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(mkdirButton)
                                        .addComponent(uploadButton)
                                        .addComponent(renameButton)
                                        .addComponent(downloadButton)
                                        .addComponent(removeButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    FTPSession getSession() {
        return session;
    }


    private void doRmdir() throws CommandFailException, SocketCloseException, ConsoleCloseException {
        if (session.getStatus() != SessionStatus.LOGGEDIN) {
            throw new CommandFailException("not logged in");
        }

        int[] rows = fileTable.getSelectedRows();
        if (rows.length == 0) {
            throw new CommandFailException("no directory selected");
        }

        for (int row : rows) {
            row = fileTable.convertRowIndexToModel(row);
            if (!FTPConfig.isDirectory((String) fileTable.getModel().getValueAt(row, 3))) {
                throw new CommandFailException("file cannot be removed");
            }
            String fileName = (String) fileTable.getModel().getValueAt(row, 0);
            String absolutePath = Paths.get(cwd, fileName).toString();
            session.handleRmdir(absolutePath);
        }
    }

    private void doDownload() throws CommandFailException, SocketCloseException, ConsoleCloseException {
        if (session.getStatus() != SessionStatus.LOGGEDIN) {
            throw new CommandFailException("not logged in");
        }

        int[] rows = fileTable.getSelectedRows();
        if (rows.length == 0) {
            throw new CommandFailException("no file selected");
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择要下载到的文件夹");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            for (int row : rows) {
                row = fileTable.convertRowIndexToModel(row);
                if (FTPConfig.isDirectory((String) fileTable.getModel().getValueAt(row, 3))) {
                    throw new CommandFailException("directory cannot be downloaded");
                }
                long totalSize = (long) fileTable.getModel().getValueAt(row, 6);
                String fileName = (String) fileTable.getModel().getValueAt(row, 0);
                String absolutePath = Paths.get(cwd, fileName).toString();
                File targetFile = chooser.getSelectedFile().toPath().resolve(fileName).toFile();
                long downloaded = 0;
                if (targetFile.exists()) {
                    int res = JOptionPane.showConfirmDialog(this, "检测到原文件已经存在，是否断点续传？", "断点续传", JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        downloaded = targetFile.length();
                    }
                }
                TransferTask task = new TransferTask(fileName, targetFile.toString(), absolutePath, "RETR", downloaded, totalSize);
                scheduler.addTask(task);
            }
        }
    }

    private void doUpload() throws CommandFailException, SocketCloseException, ConsoleCloseException {
        if (session.getStatus() != SessionStatus.LOGGEDIN) {
            throw new CommandFailException("not logged in");
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            for (File file : files) {
                long totalSize = file.length();
                long downloaded = 0;
                String fileName = file.getName();
                String targetPath = Paths.get(cwd).resolve(fileName).toString();
                TransferTask task = new TransferTask(fileName, file.toString(), targetPath, "STOR", downloaded, totalSize);
                scheduler.addTask(task);
            }
        }
    }

    @Override
    public void refreshPwd(String wd) {
        this.addressText.setText(wd);
        this.cwd = wd;
    }

    private void doRefresh() throws CommandFailException, ConsoleCloseException, SocketCloseException {
        if (session.getStatus() != SessionStatus.LOGGEDIN) {
            throw new CommandFailException("not logged in");
        }
        if (scheduler.getSize() != 0) {
            throw new CommandFailException("still have unfinished tasks");
        }
        session.handlePwd("");
        session.handleLs(cwd);
    }

    private void doRoute(String path) throws SocketCloseException, ConsoleCloseException, CommandFailException {
        if (session.getStatus() != SessionStatus.LOGGEDIN) {
            throw new CommandFailException("not logged in");
        }
        if (scheduler.getSize() != 0) {
            throw new CommandFailException("still have unfinished tasks");
        }
        session.handleCd(path);
        doRefresh();
    }

    private void routeTo(String path) throws CommandFailException, ConsoleCloseException, SocketCloseException {
        if (session.getStatus() != SessionStatus.LOGGEDIN) {
            throw new CommandFailException("not logged in");
        }
        if (!path.startsWith("/")) {
            throw new CommandFailException("path in address bar must start with /");
        }
        if (scheduler.getSize() != 0) {
            throw new CommandFailException("still have unfinished tasks");
        }
        while (addressStack.size() > addressTop) {
            addressStack.remove(addressStack.size() - 1);
        }
        addressStack.add(path);
        addressTop++;
        doRoute(path);
    }

    private void routeBack() throws CommandFailException, ConsoleCloseException, SocketCloseException {
        if (session.getStatus() != SessionStatus.LOGGEDIN) {
            throw new CommandFailException("not logged in");
        }
        if (addressTop <= 1) {
            throw new CommandFailException("no previous path");
        }
        if (scheduler.getSize() != 0) {
            throw new CommandFailException("still have unfinished tasks");
        }
        String path = addressStack.get(--addressTop - 1);
        doRoute(path);
    }

    private void routeForward() throws CommandFailException, ConsoleCloseException, SocketCloseException {
        if (session.getStatus() != SessionStatus.LOGGEDIN) {
            throw new CommandFailException("not logged in");
        }
        if (addressTop == addressStack.size()) {
            throw new CommandFailException("no succeeding path");
        }
        if (scheduler.getSize() != 0) {
            throw new CommandFailException("still have unfinished tasks");
        }
        String path = addressStack.get(addressTop++);
        doRoute(path);
    }

    private void cdUp() throws SocketCloseException, ConsoleCloseException, CommandFailException {
        if (session.getStatus() != SessionStatus.LOGGEDIN) {
            throw new CommandFailException("not logged in");
        }
        if (scheduler.getSize() != 0) {
            throw new CommandFailException("still have unfinished tasks");
        }
        String path = cwd;
        Path target = Paths.get(path).resolve("..").normalize();
        routeTo(target.toString());
    }

    private void doMkdir() throws CommandFailException, ConsoleCloseException, SocketCloseException {
        if (session.getStatus() != SessionStatus.LOGGEDIN) {
            throw new CommandFailException("not logged in");
        }
        if (scheduler.getSize() != 0) {
            throw new CommandFailException("still have unfinished tasks");
        }
        String name = mkdirText.getText();
        mkdirText.setText("");
        session.handleMkdir(name);
        doRefresh();
    }

    private void doRename() throws CommandFailException, ConsoleCloseException, SocketCloseException {
        if (session.getStatus() != SessionStatus.LOGGEDIN) {
            throw new CommandFailException("not logged in");
        }
        if (scheduler.getSize() != 0) {
            throw new CommandFailException("still have unfinished tasks");
        }
        if (fileTable.getSelectedRowCount() == 0) {
            throw new CommandFailException("no directory selected");
        }
        if (fileTable.getSelectedRowCount() != 1) {
            throw new CommandFailException("select ONE directory at a time");
        }
        int row = fileTable.getSelectedRow();
        if (!FTPConfig.isDirectory((String) fileTable.getModel().getValueAt(row, 3))) {
            throw new CommandFailException("selected item not a directory");
        }
        String fileName = (String) fileTable.getModel().getValueAt(row, 0);
        String targetName = JOptionPane.showInputDialog(this, "输入目标文件夹名", fileName);
        if (targetName == null) { // cancel
            return;
        }
        session.handleRename(fileName);
        session.provideRenameTo(targetName);
        doRefresh();
    }

    private void disconnect() throws CommandFailException, ConsoleCloseException, SocketCloseException {
        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
        model.setRowCount(0);
        model = (DefaultTableModel) statusTable.getModel();
        model.setRowCount(0);
        addressText.setText("");

        addressStack.clear();
        addressTop = 0;

        scheduler.clearTasks();

        if (session.getStatus() == SessionStatus.DISCONNECTED) {
            throw new CommandFailException("already disconnected");
        }
        session.handleClose();
    }

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
        if (session.getStatus() == SessionStatus.LOGGEDIN) {
            JOptionPane.showMessageDialog(this, "已经登录了，请先断开连接", "已经登录", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String address = ipText.getText();
        String portString = portText.getText();
        String username = usernameText.getText();
        String password = new String(passwordText.getPassword());
        try {
            session.handleOpen(address);
            session.providePort(portString);
            session.handleUser(username);
            session.providePassword(password);
            session.handleBinary();
            routeTo("/");
        } catch (SocketCloseException | ConsoleCloseException | CommandFailException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "连接失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        }

    }//GEN-LAST:event_connectButtonActionPerformed

    private void portPasvButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portPasvButtonActionPerformed
        try {
            session.handlePass();
        } catch (ConsoleCloseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        }
    }//GEN-LAST:event_portPasvButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        try {
            doRefresh();
        } catch (SocketCloseException | ConsoleCloseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "刷新失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        } catch (CommandFailException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "刷新失败", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void gotoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoButtonActionPerformed
        try {
            routeTo(addressText.getText());
        } catch (SocketCloseException | ConsoleCloseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        } catch (CommandFailException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_gotoButtonActionPerformed

    private void forwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardButtonActionPerformed
        try {
            routeForward();
        } catch (SocketCloseException | ConsoleCloseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        } catch (CommandFailException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_forwardButtonActionPerformed

    private void cdUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cdUpButtonActionPerformed
        try {
            cdUp();
        } catch (SocketCloseException | ConsoleCloseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        } catch (CommandFailException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_cdUpButtonActionPerformed

    private void disconnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectButtonActionPerformed
        try {
            disconnect();
        } catch (SocketCloseException | ConsoleCloseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "断开连接失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        } catch (CommandFailException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "断开连接失败", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_disconnectButtonActionPerformed

    private void backwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backwardButtonActionPerformed
        try {
            routeBack();
        } catch (SocketCloseException | ConsoleCloseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        } catch (CommandFailException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_backwardButtonActionPerformed

    private void mkdirButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mkdirButtonActionPerformed
        try {
            doMkdir();
        } catch (SocketCloseException | ConsoleCloseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        } catch (CommandFailException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_mkdirButtonActionPerformed

    private void renameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameButtonActionPerformed
        try {
            doRename();
        } catch (SocketCloseException | ConsoleCloseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        } catch (CommandFailException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "切换路径失败", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_renameButtonActionPerformed

    private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        try {
            doDownload();
        } catch (SocketCloseException | ConsoleCloseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "下载失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        } catch (CommandFailException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "下载失败", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_downloadButtonActionPerformed

    private void uploadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadButtonActionPerformed
        try {
            doUpload();
        } catch (SocketCloseException | ConsoleCloseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "上传失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        } catch (CommandFailException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "上传失败", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_uploadButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        try {
            doRmdir();
        } catch (SocketCloseException | ConsoleCloseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "删除文件夹失败", JOptionPane.ERROR_MESSAGE);
            try {
                disconnect();
            } catch (CommandFailException | ConsoleCloseException | SocketCloseException ignored) {
            }
        } catch (CommandFailException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "删除文件夹失败", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FtpGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FtpGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FtpGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FtpGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FtpGui().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField addressText;
    private javax.swing.JButton backwardButton;
    private javax.swing.JButton cdUpButton;
    private javax.swing.JButton connectButton;
    private javax.swing.JTextPane consoleText;
    private javax.swing.JButton disconnectButton;
    private javax.swing.JButton downloadButton;
    private javax.swing.JTable fileTable;
    private javax.swing.JButton forwardButton;
    private javax.swing.JButton gotoButton;
    private javax.swing.JTextField ipText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton mkdirButton;
    private javax.swing.JTextField mkdirText;
    private javax.swing.JPasswordField passwordText;
    private javax.swing.JButton portPasvButton;
    private javax.swing.JTextField portText;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton renameButton;
    private javax.swing.JTable statusTable;
    private javax.swing.JButton uploadButton;
    private javax.swing.JTextField usernameText;
    // End of variables declaration//GEN-END:variables
}
