package ftpclient;

class TransferTask {
    String fileName;
    String localPath;
    String remotePath;
    String command;
    long downloaded;
    long total;
    int row;

    TransferTask(String fileName, String localPath, String remotePath, String command, long downloaded, long total) {
        this.fileName = fileName;
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.command = command;
        this.downloaded = downloaded;
        this.total = total;
    }
}
