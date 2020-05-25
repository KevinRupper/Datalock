package ui;

import java.io.File;

public class UIMyFile {
    public final File mFile;

    public UIMyFile(final File pFile) {
        mFile = pFile;
    }

    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    public UIMyFile[] listFiles() {
        final File[] files = mFile.listFiles();
        if (files == null) return null;
        if (files.length < 1) return new UIMyFile[0];

        final UIMyFile[] ret = new UIMyFile[files.length];
        for (int i = 0; i < ret.length; i++) {
            final File f = files[i];
            ret[i] = new UIMyFile(f);
        }
        return ret;
    }

    public File getFile() {
        return mFile;
    }

    @Override public String toString() {
        return mFile.getName();
    }
}
