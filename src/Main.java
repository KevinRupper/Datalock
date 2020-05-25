import javax.swing.*;

/**
 * Entry point of the App
 */
public class Main {
    public static void main(String[] args) {

        //noinspection ResultOfMethodCallIgnored
        DataLockSingleton.getInstance();

        Login login = new Login();

        JFrame loginFrame = new JFrame("Login");
        loginFrame.setContentPane(login.panel);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.pack();
        loginFrame.setVisible(true);

        login.setLoginListener(new LoginListenerImp(loginFrame));
    } // End static main

} // End main

