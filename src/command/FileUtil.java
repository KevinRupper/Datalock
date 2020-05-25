package command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {

    static public void writeFile(String fileName, byte[] bytes) throws IOException {
        File file = new File(fileName);
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }

    static public byte[] readFile(String name) throws IOException {
        File file = new File(name);
        FileInputStream inputStream = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        //noinspection ResultOfMethodCallIgnored
        inputStream.read(fileBytes);
        inputStream.close();
        return fileBytes;
    }

    static public byte[] readFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        //noinspection ResultOfMethodCallIgnored
        inputStream.read(fileBytes);
        inputStream.close();
        return fileBytes;
    }
}
