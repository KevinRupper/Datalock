import command.DecryptCommand;
import command.EncryptCommand;
import command.ShareCommand;
import http.RestHelper;
import org.json.simple.JSONObject;
import ui.*;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.security.PublicKey;
import java.util.Base64;

public class UI {

    private enum GUEST_OS{
        WINDOWS, MAC, LINUX,
    }

    // General UI
    public JPanel mainPanel;
    private JTabbedPane tabbedPane;

    // Encryption
    private JScrollPane encryptionSourceScrollPane;
    private JScrollPane encryptionDestinationScrollPane;
    private JTree encryptionSourceTree;
    private JTree encryptionDestinationTree;
    private UIFileTreeModel encryptTreeModel;
    private UIFileTreeModel encryptTreeModelDestination;
    private JButton encryptionButton;

    // Decryption
    private JScrollPane decryptionSourceScrollPane;
    private JScrollPane decryptionDestinationScrollPane;
    private JTree decryptionSourceTree;
    private JTree decryptionDestinationTree;
    private UIFileTreeModel decryptionTreeModel;
    private UIFileTreeModel decryptionTreeModelDestination;
    private JButton decryptionButton;

    // Share
    private JTextField shareTextField;
    private JTree shareTree;
    private UIFileTreeModel shareTreeModel;
    private JButton shareButton;
    private JScrollPane shareScrollPanel;
    private JButton exitButton;


    private UIMyFile sourceNode;
    private UIMyFile destinationNode;


    public UI() {
        encryptionButton.addActionListener(e -> {
            if(sourceNode != null && destinationNode != null) {
                if(destinationNode.isDirectory()) {
                    EncryptCommand encryptCommand = new EncryptCommand();
                    encryptCommand.setSource(sourceNode.getFile().getPath());
                    encryptCommand.setDestination(destinationNode.getFile().getPath());
                    encryptCommand.setPublicKey(DataLockSingleton.getInstance().keyPair.getPublic());
                    encryptCommand.encrypt();
                    encryptTreeModel.reload();
                    encryptTreeModelDestination.reload();

                }
                else {
                    // TODO: show error that tell user
                    // TODO: that the destination is not a folder
                    JOptionPane.showMessageDialog(null, "The destination must be a folder");
                }
            }
            else {
                String message = "";

                if(sourceNode == null)
                    message = "Select file";
                else if(destinationNode == null)
                    message = "Select destination";

                JOptionPane.showMessageDialog(null, message);
            }
        });

        decryptionButton.addActionListener(e -> {
            if(sourceNode != null && destinationNode != null) {
                if(destinationNode.isDirectory()) {
                    DecryptCommand decryptCommand = new DecryptCommand();
                    decryptCommand.setSource(sourceNode.getFile().getPath());
                    decryptCommand.setDestination(destinationNode.getFile().getPath());
                    decryptCommand.setKey(DataLockSingleton.getInstance().keyPair.getPrivate());
                    boolean success = decryptCommand.decrypt();
                    decryptionTreeModel.reload();
                    decryptionTreeModelDestination.reload();

                    if(success)
                        JOptionPane.showMessageDialog(null, "File ciphered correclty");
                    else
                        JOptionPane.showMessageDialog(null, "Error");
                }
                else {
                    // TODO: show error that tell user
                    // TODO: that the destination is not a folder
                    JOptionPane.showMessageDialog(null, "The destination must be a folder");
                }
            }
            else {
                String message = "";

                if(sourceNode == null)
                    message = "Select file to decipher";
                else if(destinationNode == null)
                    message = "Select destination";

                JOptionPane.showMessageDialog(null, message);
            }
        });

        shareButton.addActionListener(e -> {
            if (sourceNode != null) {
                if (!sourceNode.isDirectory() && sourceNode.getFile().getName().matches(".*.ne")) {
                    JSONObject response = RestHelper.searchUser(shareTextField.getText());

                    if(response == null || response.size() == 0) {
                        JOptionPane.showMessageDialog(null, "The user not exists");
                        return;
                    }

                    byte[] b64Public = Base64.getDecoder().decode(response.get("publicKey").toString());
                    PublicKey publicKey = RSAHelper.readX509PublicKey(b64Public);
                    ShareCommand shareCommand = new ShareCommand();
                    shareCommand.setPrivateKey(DataLockSingleton.getInstance().keyPair.getPrivate());
                    shareCommand.setPublicKey(publicKey);
                    shareCommand.setFile(sourceNode.getFile().getPath());
                    shareCommand.shareFile();

                    JOptionPane.showMessageDialog(null, "You can now share the file with: " + shareTextField.getText());

                } else {
                    JOptionPane.showMessageDialog(null, "You must select a file of type *.ne");
                }
            }
        });

        tabbedPane.addChangeListener(e -> {
            // Clean paths
            sourceNode = null;
            destinationNode = null;
        });
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private void createUIComponents() {
        GUEST_OS os = detectOS();

        // Encryption
        createEncryptionComponents(os);

        // Decryption
        createDecryptionComponents(os);

        // Share
        createShareComponents(os);
    }

    private void createEncryptionComponents(GUEST_OS os) {
        UIMyFile mf = new UIMyFile(getRootFile(os));
        encryptTreeModel = new UIFileTreeModel(mf);
        encryptionSourceTree = new JTree(encryptTreeModel);
        encryptionSourceTree.setEditable(true);

        encryptionSourceTree.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            sourceNode = (UIMyFile) path.getLastPathComponent();
            System.out.println("You selected source:" + sourceNode.mFile.getPath());
        });

        encryptionSourceScrollPane = (new JScrollPane(encryptionSourceTree));

        UIMyFile mfd = new UIMyFile(getRootFile(os));
        encryptTreeModelDestination = new UIFileTreeModel(mfd);
        encryptionDestinationTree = new JTree(encryptTreeModelDestination);
        encryptionDestinationTree.setEditable(true);

        encryptionDestinationTree.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            destinationNode = (UIMyFile) path.getLastPathComponent();
            System.out.println("You selected destination:" + destinationNode.mFile.getPath());
        });

        encryptionDestinationScrollPane = (new JScrollPane(encryptionDestinationTree));
    }

    private void createDecryptionComponents(GUEST_OS os) {
        UIMyFile mf = new UIMyFile(getRootFile(os));
        decryptionTreeModel = new UIFileTreeModel(mf);
        decryptionSourceTree = new JTree(decryptionTreeModel);
        decryptionSourceTree.setEditable(true);

        decryptionSourceTree.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            sourceNode = (UIMyFile) path.getLastPathComponent();
            System.out.println("You selected source:" + sourceNode.mFile.getPath());
        });

        decryptionSourceScrollPane = (new JScrollPane(decryptionSourceTree));

        UIMyFile mfd = new UIMyFile(getRootFile(os));
        decryptionTreeModelDestination = new UIFileTreeModel(mfd);
        decryptionDestinationTree = new JTree(decryptionTreeModelDestination);
        decryptionDestinationTree.setEditable(true);

        decryptionDestinationTree.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            destinationNode = (UIMyFile) path.getLastPathComponent();
            System.out.println("You selected destination:" + destinationNode.mFile.getPath());
        });

        decryptionDestinationScrollPane = (new JScrollPane(decryptionDestinationTree));
    }

    private void createShareComponents(GUEST_OS os){
        UIMyFile mf = new UIMyFile(getRootFile(os));
        shareTreeModel = new UIFileTreeModel(mf);
        shareTree = new JTree(shareTreeModel);
        shareTree.setEditable(true);

        shareTree.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            sourceNode = (UIMyFile) path.getLastPathComponent();
            System.out.println("You selected source:" + sourceNode.mFile.getPath());
        });

        shareScrollPanel = (new JScrollPane(shareTree));
    }

    private File getRootFile(GUEST_OS os) {
        if(os == GUEST_OS.MAC)
            return new File("/");
        else if(os == GUEST_OS.LINUX)
            return new File("/");
        else
            return new File("C:\\");
    }

    private GUEST_OS detectOS() {
        String os = System.getProperty("os.name").toLowerCase();

        if(os.contains("windows"))
            return GUEST_OS.WINDOWS;
        else if(os.contains("mac"))
            return GUEST_OS.MAC;
        else if(os.contains("linux"))
            return GUEST_OS.LINUX;

        return GUEST_OS.WINDOWS;
    }
}
