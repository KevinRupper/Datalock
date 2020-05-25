package command;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class FileHeader {
    public SecretKey secretKey;
    public IvParameterSpec ivSpec;
    public int nameFileLength;

    FileHeader(SecretKey secretKey, IvParameterSpec iv, int nameFileLength) {
        this.secretKey = secretKey;
        this.ivSpec = iv;
        this.nameFileLength = nameFileLength;
    }
}
