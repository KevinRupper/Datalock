import command.RSAPairCommand;
import http.RestHelper;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.json.simple.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

public class Login {

    private static final byte[] IV = { 20, 2, 1, 3, 4, 34, 20, 20, 45, 8, 39, 22, 3, 32, 2, 2 };

    public JPanel panel;
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton newUserButton;

    private LoginListener loginListener;

    public void setLoginListener(LoginListener loginListener) {
        this.loginListener = loginListener;
    }


    public Login() {
        loginButton.addActionListener(e -> {
            // Do password hash from user with salt
            // and sent it to the server in order to
            // verify it.
            String salt = "DataLock password:";
            String input = Arrays.toString(passwordField.getPassword()) + salt;
            SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
            byte[] digest = digestSHA3.digest(input.getBytes());
            String b64Password = Base64.getEncoder().encodeToString(digest);

            JSONObject response = RestHelper.doLogin(userTextField.getText(), b64Password);

            if(response != null && response.size() != 0) {
                try{
                    // Obtain RSA Keys from the server response
                    byte[] b64Public = Base64.getDecoder().decode(response.get("clavePublica").toString());
                    byte[] b64EncryptedPrivate = Base64.getDecoder().decode(response.get("clavePrivada").toString());

                    // Obtain SHA3 from password with RSA salt
                    byte[] passwordSHA3Digest = getPasswordSHA3RSASalt();

                    // Decrypt private key with password digest
                    IvParameterSpec ivspec = new IvParameterSpec(IV);
                    SecretKeySpec secretKeySpec = new SecretKeySpec(passwordSHA3Digest, "AES");
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);
                    byte[] b64Private = cipher.doFinal(b64EncryptedPrivate);
                    b64Private = Base64.getDecoder().decode(b64Private);

                    PublicKey publicKey = RSAHelper.readX509PublicKey(b64Public);
                    PrivateKey privateKey = RSAHelper.readPKCS8PrivateKey(b64Private);
                    DataLockSingleton.getInstance().keyPair = new KeyPair(publicKey, privateKey);

                    loginListener.loginDone(userTextField.getText());
                }
                 catch (NoSuchAlgorithmException |
                         InvalidKeyException |
                         NoSuchPaddingException |
                         BadPaddingException |
                         IllegalBlockSizeException |
                         InvalidAlgorithmParameterException e1) {

                    e1.printStackTrace();
                     JOptionPane.showMessageDialog(null, "Invalid credentials");
                }
            }
            else {
                JOptionPane.showMessageDialog(null, "Invalid credentials");
            }
        });

        newUserButton.addActionListener(e -> {

            if(userTextField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Complete the user");
                return;
            }

            if(passwordField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(null, "Complete the password");
                return;
            }

            if(passwordField.getPassword().length < 10) {
                JOptionPane.showMessageDialog(null, "The password must be at least 10 characters");
                return;
            }

            String passwordSalt = "DataLock password:";
            String user = userTextField.getText();
            String password = Arrays.toString(passwordField.getPassword());

            // Create RSA keys for the new user
            RSAPairCommand pairCommand = new RSAPairCommand();
            KeyPair keyPair = pairCommand.createRSAPair();

            // Store new keys
            DataLockSingleton.getInstance().keyPair = keyPair;
            DataLockSingleton.getInstance().password = password;

            // Create hash from password
            String input = Arrays.toString(passwordField.getPassword()) + passwordSalt;
            SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
            byte[] digest = digestSHA3.digest(input.getBytes());

            String b64PublicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String b64PrivateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            String b64Password = Base64.getEncoder().encodeToString(digest);

            // Encrypt private key
            try {
                // SHA3-256 from password with RSA salt
                String rsaPrivateKeySalt = "Datalock private key:";
                String passwordSaltRSA = Arrays.toString(passwordField.getPassword()) + rsaPrivateKeySalt;
                SHA3.DigestSHA3 rsaDigestSHA3 = new SHA3.Digest256();
                byte[] rsaDigest = rsaDigestSHA3.digest(passwordSaltRSA.getBytes());

                // Encrypt RSA key using the result of the SHA3-256
                IvParameterSpec ivspec = new IvParameterSpec(IV);
                SecretKeySpec secretKeySpec = new SecretKeySpec(rsaDigest, "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);
                byte[] privateKeyEncrypted = cipher.doFinal(b64PrivateKey.getBytes());
                String pb64PrivateKeyEncrypted =  Base64.getEncoder().encodeToString(privateKeyEncrypted);

                boolean result = RestHelper.createUser(
                        user,
                        b64Password,
                        b64PublicKey,
                        pb64PrivateKeyEncrypted);

                if(result) {
                    loginListener.loginDone(userTextField.getText());
                }
                else {
                    JOptionPane.showMessageDialog(null, "Error creating user");
                }

            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e1) {

                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error creating user");
            }
        });
    }

    private byte[] getPasswordSHA3RSASalt() {
        // SHA3-256 from password with RSA salt
        String rsaPrivateKeySalt = "Datalock private key:";
        String passwordSaltRSA = Arrays.toString(passwordField.getPassword()) + rsaPrivateKeySalt;
        SHA3.DigestSHA3 rsaDigestSHA3 = new SHA3.Digest256();
        return rsaDigestSHA3.digest(passwordSaltRSA.getBytes());
    }
}
