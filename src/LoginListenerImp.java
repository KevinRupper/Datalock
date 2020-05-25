import javax.swing.*;

public class LoginListenerImp implements LoginListener {

    private JFrame loginFrame;

    public LoginListenerImp(JFrame loginFrame) {
        this.loginFrame = loginFrame;
    }

    @Override
    public void loginDone(String user) {
        JFrame frame = new JFrame("DataLock - " + user);
        frame.setContentPane(new UI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        loginFrame.dispose();
    }
}
