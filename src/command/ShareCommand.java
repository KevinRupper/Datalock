package command;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

public class ShareCommand extends Command {

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String file;

    public ShareCommand() {
        this.type = CommandType.share;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void shareFile() {
        File f = new File(this.file);
        if (f.exists()) {
            if (!f.isDirectory())
                encryptNewHeader(f);

            // TODO: error, its a folder
        }
    }

    private void encryptNewHeader(File file) {
        try {
            byte[] fileBytes = FileUtil.readFile(file);

            // ========================================
            // Extract number of headers from the file
            int numberOfHeaders = new BigInteger(Arrays.copyOfRange(fileBytes,
                    0,
                    Constants.HEADERS_NUMBER_FLAG_LENGTH)).intValue();

            // ========================================
            // Remove first bytes
            byte[] data = Arrays.copyOfRange(fileBytes, Constants.HEADERS_NUMBER_FLAG_LENGTH, fileBytes.length);

            // ========================================
            // Extract owner header
            byte[] mainHeader = byteRange(data,
                    (numberOfHeaders - 1) * Constants.HEADER_LENGTH,
                    (numberOfHeaders - 1) * Constants.HEADER_LENGTH + Constants.HEADER_LENGTH);

            // ========================================
            // Decrypt header with owner private key
            byte[] decryptedHeader = decryptHeader(mainHeader);

            // ========================================
            // Encrypt new header with shared public key
            if(decryptedHeader != null) {
                byte[] newEncryptedHeader = encryptHeader(decryptedHeader);

                if(newEncryptedHeader != null) {
                    byte[] numberHeaders = new byte[Constants.HEADERS_FLAG_SIZE];
                    ByteBuffer.wrap(numberHeaders).putInt(numberOfHeaders + 1);

                    // ========================================
                    // Append new header to original file
                    FileOutputStream outputStream = new FileOutputStream(file, false);
                    outputStream.write(numberHeaders);
                    outputStream.write(newEncryptedHeader);
                    outputStream.write(data);
                    outputStream.flush();
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] encryptHeader(byte[] decryptedHeader){
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(decryptedHeader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] decryptHeader(byte[] encryptedHeader) {
        try {
            // Load private key
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encryptedHeader);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] byteRange(byte[] data, int from, int to) {
        return Arrays.copyOfRange(data, from, to);
    }
}
