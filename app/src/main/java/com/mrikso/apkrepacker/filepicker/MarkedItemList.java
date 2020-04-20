
package com.mrikso.apkrepacker.filepicker;

import java.util.HashMap;


/*  SingleTon containing <Key,Value> pair of all the selected files.
 *  Key: Directory/File path.
 *  Value: FileListItem Object.
 */
public class MarkedItemList {

    private static HashMap<String, FileListItem> ourInstance = new HashMap<>();

    public static void addMultiItem(FileListItem item) {
        ourInstance.put(item.getPath(), item);
    }

    public static void removeSelectedItem(String key) {
        ourInstance.remove(key);
    }

    public static boolean hasItem(String key) {
        return ourInstance.containsKey(key);
    }

    public static void clearSelectionList() {
        ourInstance = new HashMap<>();
    }

    public static void addSingleFile(FileListItem item) {
        ourInstance.clear();
        ourInstance.put(item.getPath(), item);
    }

    public static String[] getSelectedPaths() {
        return ourInstance.keySet().toArray(new String[0]);
    }

    public static int getFileCount() {
        return ourInstance.size();
    }
}
