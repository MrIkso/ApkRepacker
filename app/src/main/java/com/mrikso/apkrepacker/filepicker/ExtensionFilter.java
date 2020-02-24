
package com.mrikso.apkrepacker.filepicker;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;


/*  Class to filter the list of files.
 */
public class ExtensionFilter implements FileFilter {

    private int selectType;
    private String[] validExtensions;

    public ExtensionFilter(int selectType, String[] extensions) {
        this.selectType = selectType;
        if (extensions != null) {
            this.validExtensions = extensions;
        } else {
            this.validExtensions = new String[]{""};
        }
    }

    /**
     * Function to filter files based on defined rules.
     */
    @Override
    public boolean accept(File file) {
        if (file.getName().startsWith(".")) {
            return false;
        } else if (file.isDirectory()) {
            // 文件夹始终显示
            return file.canRead();
        } else {
            if (selectType == FilePickerDialog.TYPE_DIR) {
                // 文件始终显示
                return true;
            } else {
                // 只显示对应的文件
                String name = file.getName().toLowerCase(Locale.getDefault());
                for (String ext : validExtensions) {
                    if (name.endsWith(ext.toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
