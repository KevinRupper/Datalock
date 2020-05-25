package command;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Arrays;

public class DecryptCommand extends Command {

    private PrivateKey privateKey;
    private String source;
    private String destination;

    public  DecryptCommand() {
        super.type = CommandType.decrypt;
    }

    public CommandType getType() {
        return type;
    }

    public void setKey(PrivateKey key) {
        this.privateKey = key;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public boolean decrypt(){
        return doDecrypt();
    }

    private boolean doDecrypt() {
        File f = new File(this.source);
        if (f.exists()) {
            if(!f.isDirectory()) {
                return decryptFile(f);
            }
            else {
                // TODO: error, only file not directories
                return false;
            }
        }
        return false;
    }

    // ===============================================================
    // Number of headers | Key + IV + Name length | Name + File |
    // ===============================================================
    //       4 bytes     | 16  + 16 +     4       |   n  +  n   |
    // ===============================================================
    private boolean decryptFile(File file) {
        try {
            // ===================================
            // Extract data from file
            byte[] fileBytes = FileUtil.readFile(file);
            int numberOfHeaders = new BigInteger(Arrays.copyOfRange(fileBytes, 0, Constants.HEADERS_NUMBER_FLAG_LENGTH)).intValue();
            byte[] data = Arrays.copyOfRange(fileBytes, Constants.HEADERS_NUMBER_FLAG_LENGTH, fileBytes.length);

            // ===================================
            // Iterate over the headers
            Header header = null;
            for(int i = 0; i <= numberOfHeaders && header == null; i++) {
                byte[] encryptedHeader = byteRange(data,i * Constants.HEADER_LENGTH,  Constants.HEADER_LENGTH + (i * Constants.HEADER_LENGTH));
                // Decrypt header
                header = decryptHeader(encryptedHeader);
            }

            // ===================================
            // Extract the body
            if(header != null) {
                byte[] encryptedBody = byteRange(data, Constants.HEADER_LENGTH * numberOfHeaders, data.length);
                // Decrypt body
                Body body = decryptBody(header, encryptedBody);

                // Create the file
                FileUtil.writeFile(destination + File.separator + body.nameFile + ".decrypted", body.body);
                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static byte[] byteRange(byte[] data, int from, int to) {
       return Arrays.copyOfRange(data, from, to);
    }

    private Header decryptHeader(byte[] encryptedHeader) {
        try {
            // Decrypt header with private key
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedHeader = cipher.doFinal(encryptedHeader);

            // Extract: secretKey, IV and name file length
            byte[] secretKeyBytes = Arrays.copyOfRange(decryptedHeader, 0, 16);
            byte[] ivXOR = Arrays.copyOfRange(decryptedHeader, 16, 32);
            byte[] fileNameLength = Arrays.copyOfRange(decryptedHeader, 32, 36);
            int nameFileLength = new BigInteger(Arrays.copyOfRange(fileNameLength, 0, 4)).intValue();

            //noinspection MismatchedReadAndWriteOfArray
            byte[] ivString = Constants.IV_XOR_STRING.getBytes();
            byte[] iv = new byte[Constants.IV_XOR_STRING.length()];

            for(int i=0; i<ivXOR.length; i++)
                iv[i] = (byte)(0xff & ((int)ivXOR[i]) ^ ((int)ivString[i]));

            SecretKey secretKey = new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            return new Header(secretKey, ivSpec, nameFileLength);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Body decryptBody(Header header, byte[] encryptedBody) throws
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException {

        // Use the secret key to decrypt the original file
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, header.secretKey, header.ivSpec);
        byte[] decryptedBody = cipher.doFinal(encryptedBody);

        // Extract name of the file and the file data
        byte[] nameFile = Arrays.copyOfRange(decryptedBody,0, header.nameFileLength);
        byte[] body = Arrays.copyOfRange(decryptedBody, header.nameFileLength, decryptedBody.length);

        String fileName = new String(nameFile, StandardCharsets.UTF_8);

        return new Body(fileName, body);
    }

    public class Header {
        private SecretKey secretKey;
        private IvParameterSpec ivSpec;
        private int nameFileLength;

        Header( SecretKey secretKey, IvParameterSpec iv, int nameFileLength) {
            this.secretKey = secretKey;
            this.ivSpec = iv;
            this.nameFileLength = nameFileLength;
        }
    }

    private class Body {
        private String nameFile;
        private byte[] body;

        Body(String nameFile, byte[] body) {
            this.nameFile = nameFile;
            this.body = body;
        }
    }
}
