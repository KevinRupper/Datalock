package command;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAPairCommand extends Command {

    private static final int KEY_SIZE = 3072;

    public RSAPairCommand () {
        super.type = CommandType.rsa;
    }

    public KeyPair createRSAPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(KEY_SIZE);
            return kpg.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
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

    static PublicKey readX509PublicKey(String key) {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(key.getBytes());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    static PrivateKey readPKCS8PrivateKey(String key) {
        try {
            /* Generate private key. */
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(key.getBytes());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(ks);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }
}
