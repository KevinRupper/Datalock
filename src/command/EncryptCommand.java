package command;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.SecureRandom;

public class EncryptCommand extends Command {

    private PublicKey publicKey;
    private String source;
    private String destination;

    public  EncryptCommand() {
        super.type = CommandType.encrypt;
    }

    public CommandType getType() {
        return type;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void encrypt(){
        doCrypt();
    }

    private void doCrypt() {
        File f = new File(this.source);
        if (f.exists()) {
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                if (files != null) {
                    for (File file : files)
                        encryptFile(file);

                }
            } else {
                encryptFile(f);
            }
        }
    }

    // ===============================================================
    // Number of headers | Key + IV + Name length | Name + File |
    // ===============================================================
    //       4 bytes     | 16  + 16 +     4       |   n  +  n   |
    // ===============================================================
    private void encryptFile(File file) {
        try {
            // ===================================
            // Generate symmetric key
            SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();

            // Create IV vector
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            byte[] iv = new byte[cipher.getBlockSize()];
            secureRandom.nextBytes(iv);

            byte[] ivString = Constants.IV_XOR_STRING.getBytes();
            byte[] ivXOR = new byte[Constants.IV_XOR_STRING.length()];

            for(int i=0; i<iv.length; i++)
                ivXOR[i] = (byte)(0xff & ((int)iv[i]) ^ ((int)ivString[i]));

            // ===================================
            // Build header and body
            byte[] fileName = file.getName().getBytes(StandardCharsets.UTF_8);
            byte[] header = buildHeader(secretKey, ivXOR, fileName.length);
            byte[] body = buildBody(fileName, FileUtil.readFile(file));

            // ===================================
            // Encrypt body
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            byte[] encryptedBody = cipher.doFinal(body);

            // ===================================
            // Encrypt header with the public publicKey
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedHeader = cipher.doFinal(header);

            // ===================================
            // Create final source
            byte[] combined = buildFinalFile(encryptedHeader, encryptedBody);

            // ===================================
            // Write the encrypted source
            FileUtil.writeFile(destination + File.separator + this.randomIdentifier() + ".ne", combined);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] buildHeader(SecretKey secretKey, byte[] iv, int nameFileLength) {
        byte[] header = new byte[Constants.HEADER_SIZE];
        byte[] nameLength = new byte[Constants.HEADERS_NAME_SIZE];
        ByteBuffer.wrap(nameLength).putInt(nameFileLength);

        // 16 bytes SecretKey
        System.arraycopy(secretKey.getEncoded(),0, header,0 , secretKey.getEncoded().length);
        // 16 bytes IV
        System.arraycopy(iv,0, header, secretKey.getEncoded().length, iv.length);
        // 4 bytes name length
        System.arraycopy(nameLength,0, header, secretKey.getEncoded().length + iv.length, nameLength.length);

        return header;
    }

    private byte[] buildBody(byte[] fileName, byte[] fileBytes) {
        byte[] body = new byte[fileName.length + fileBytes.length];
        System.arraycopy(fileName,0, body,0 , fileName.length);
        System.arraycopy(fileBytes,0, body, fileName.length , fileBytes.length);

        return body;
    }

    private byte[] buildFinalFile(byte[] encryptedHeader, byte[] encryptedBody) {
        byte[] combined = new byte[Constants.HEADERS_FLAG_SIZE + encryptedHeader.length + encryptedBody.length];
        byte[] numberHeaders = new byte[Constants.HEADERS_FLAG_SIZE];
        ByteBuffer.wrap(numberHeaders).putInt(1);

        System.arraycopy(numberHeaders,0, combined,0 , numberHeaders.length);
        System.arraycopy(encryptedHeader,0, combined,  numberHeaders.length, encryptedHeader.length);
        System.arraycopy(encryptedBody,0, combined, encryptedHeader.length + numberHeaders.length, encryptedBody.length);

        return combined;
    }

    private String randomIdentifier() {
        String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";
        int stringLength = 30;
        StringBuilder builder = new StringBuilder();
        while (stringLength-- != 0) {
            int character = (int)(Math.random()*lexicon.length());
            builder.append(lexicon.charAt(character));
        }
        return builder.toString();
    }
}
