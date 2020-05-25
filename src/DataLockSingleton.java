import java.security.KeyPair;

public class DataLockSingleton {

    public KeyPair keyPair;
    public String password;

    private static final DataLockSingleton INSTANCE = new DataLockSingleton();

    private DataLockSingleton(){}

    public static DataLockSingleton getInstance() {
        return INSTANCE;
    }
}
