package com.mrikso.apkrepacker.patchengine;

import com.jecelyin.common.utils.IOUtils;
import com.mrikso.apkrepacker.utils.FileUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class RemoveFilesRule extends Core {

    private List<String> files = new ArrayList();

    RemoveFilesRule(){

    }

    @Override
    public void start(LineReader lineReader) {
        line = lineReader.getLine();
        String readLine = lineReader.readLine();
        while (readLine != null) {
            String trim = readLine.trim();
            if ("[/REMOVE_FILES]".equals(trim)) {
                return;
            }
            if (super.checkName(trim, lineReader)) {
                readLine = lineReader.readLine();
            } else if ("TARGET:".equals(trim)) {
                while (true) {
                    readLine = lineReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    readLine = readLine.trim();
                    if (readLine.startsWith("[")) {
                        break;
                    } else if (!"".equals(readLine)) {
                        files.add(readLine);
                    }
                }
            } else {
               // bVar.a((int) R.string.patch_error_cannot_parse, Integer.valueOf(cVar.a()), trim);
                readLine = lineReader.readLine();
            }
        }

    }

    @Override
    public boolean inSmali() {
        for (String file : files) {
            if (Core.checkIsSmali(file)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String currentRule(ZipFile zipFile) {
        for (int i = 0; i < files.size(); i++) {
            String str = FileUtil.getProjectPath() + "/" + files.get(i);
            File delete = new File(str);
           // int lastIndexOf = str.lastIndexOf(47);
           // String substring = str.substring(0, lastIndexOf);
            //String substring2 = str.substring(lastIndexOf + 1);
            if (delete.exists()) {
                if (delete.isFile()) {
                    try {
                        FileUtils.deleteQuietly(delete);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        FileUtils.deleteDirectory(delete);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }
}
