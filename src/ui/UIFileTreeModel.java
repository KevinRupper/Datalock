package ui;

import java.io.File;
import java.util.ArrayList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class UIFileTreeModel implements TreeModel {
    public final ArrayList<TreeModelListener>  mListeners  = new ArrayList<>();
    public final UIMyFile mFile;

    public UIFileTreeModel(final UIMyFile pFile) {
        mFile = pFile;
    }

    @Override public Object getRoot() {
        return mFile;
    }

    @Override public Object getChild(final Object pParent, final int pIndex) {
        return ((UIMyFile) pParent).listFiles()[pIndex];
    }

    @Override public int getChildCount(final Object pParent) {
        return ((UIMyFile) pParent).listFiles().length;
    }

    @Override public boolean isLeaf(final Object pNode) {
        return !((UIMyFile) pNode).isDirectory();
    }

    @Override public void valueForPathChanged(final TreePath pPath, final Object pNewValue) {
        final UIMyFile oldTmp = (UIMyFile) pPath.getLastPathComponent();
        final File oldFile = oldTmp.getFile();
        final String newName = (String) pNewValue;
        final File newFile = new File(oldFile.getParentFile(), newName);
        oldFile.renameTo(newFile);
        System.out.println("Renamed '" + oldFile + "' to '" + newFile + "'.");
        reload();
    }

    @Override public int getIndexOfChild(final Object pParent, final Object pChild) {
        final UIMyFile[] files = ((UIMyFile) pParent).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i] == pChild) return i;
        }
        return -1;
    }

    @Override public void addTreeModelListener(final TreeModelListener pL) {
        mListeners.add(pL);
    }

    @Override public void removeTreeModelListener(final TreeModelListener pL) {
        mListeners.remove(pL);
    }

    public void reload() {
        // Need to duplicate the code because the root can formally be
        // no an instance of the TreeNode.
        final int n = getChildCount(getRoot());
        final int[] childIdx = new int[n];
        final Object[] children = new Object[n];

        for (int i = 0; i < n; i++) {
            childIdx[i] = i;
            children[i] = getChild(getRoot(), i);
        }

        fireTreeStructureChanged(this, new Object[] { getRoot() }, childIdx, children);
    }

    public void fireTreeStructureChanged(final Object source, final Object[] path, final int[] childIndices, final Object[] children) {
        final TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
        for (final TreeModelListener l : mListeners) {
            l.treeStructureChanged(event);
        }
    }
}
