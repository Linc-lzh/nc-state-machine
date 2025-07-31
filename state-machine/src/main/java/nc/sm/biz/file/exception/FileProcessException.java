package nc.sm.biz.file.exception;

public class FileProcessException extends Exception{
    public FileProcessException(String message) {
        super(message);
    }

    public FileProcessException(String message, Exception ie) {
    }
}
