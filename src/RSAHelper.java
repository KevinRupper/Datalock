import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAHelper {

    static PublicKey readX509PublicKey(byte[] key) {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    static PrivateKey readPKCS8PrivateKey(byte[] key) {
        try {
            /* Generate private key. */
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(key);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(ks);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    static PrivateKey readPKCS8PrivateKeyFromFile(String keyFile) {
        try {
            /* Generate private key. */
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(readFileBytes(keyFile));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(ks);

        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    static PublicKey readX509PublicKeyFromFile(String keyFile) {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(readFileBytes(keyFile));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private static byte[] readFileBytes(String filename) throws IOException {
        File f = new File(filename);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] fileBytes = new byte[(int)f.length()];
        dis.readFully(fileBytes);
        dis.close();
        return fileBytes;
    }

}
