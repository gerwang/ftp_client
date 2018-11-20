package ftpclient;

public interface TransferListener {
    void onProgress(int delta);

    void onError();

    void onDone();
}
