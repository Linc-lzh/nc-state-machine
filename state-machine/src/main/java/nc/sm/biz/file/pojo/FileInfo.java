package nc.sm.biz.file.pojo;

public class FileInfo {
    private String remoteFilePath;
    private String remoteFileName;

    public FileInfo(String remoteFilePath, String remoteFileName) {
        this.remoteFilePath = remoteFilePath;
        this.remoteFileName = remoteFileName;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public void setRemoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
    }

    public String getRemoteFileName() {
        return remoteFileName;
    }

    public void setRemoteFileName(String remoteFileName) {
        this.remoteFileName = remoteFileName;
    }
}
