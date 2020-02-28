package com.mrikso.apkrepacker.patchengine;

import android.util.Log;

import com.jecelyin.common.utils.IOUtils;
import com.mrikso.apkrepacker.utils.FileUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AddFilesRule extends Core {
    private String source;
    private String target;
    private boolean extract;

    /*
        Парсим нужные значения для патчинга
     */
    @Override
    public void start(LineReader lineReader) {
        line  = lineReader.getLine();
        String readLine = lineReader.readLine();
        while (readLine != null) {
            String trim = readLine.trim();
            if ("[/ADD_FILES]".equals(trim)) {
                break;
            } else if (super.checkName(trim, lineReader)) {
                readLine = lineReader.readLine();
            } else {
                switch (trim) {
                    case "SOURCE:":
                        source = lineReader.readLine().trim();
                        break;
                    case "TARGET:":
                        target = lineReader.readLine().trim();
                        Log.i("Reader", String.format("target  %s", target));
                        break;
                    case "EXTRACT:":
                        extract = Boolean.valueOf(lineReader.readLine().trim()).booleanValue();
                        break;
                    default:
                        Log.i("Reader", String.format("Нельзя спарсить %d,  %s", Integer.valueOf(lineReader.getLine()), line));
                        break;
                }
                readLine = lineReader.readLine();
            }
        }
        if (target != null && target.endsWith("/")) {
            target = target.substring(0, target.length() - 1);
        }
    }

    public final boolean a() {
        return Core.checkIsSmali(target);
    }

    /*
        Происходит процесс патчинга
     */
    @Override
    public String currentRule(ZipFile zipFile) {
        FileOutputStream fileOutputStream;
        InputStream inputStream;
        FileOutputStream fileOutputStream2 = null;
        ZipEntry entry = zipFile.getEntry(source);
        if (entry == null) {
            Log.i("Reader","патч пустой");
        } else {
            try {
                inputStream = zipFile.getInputStream(entry);
                IOUtils.copyFile(inputStream, new FileOutputStream(FileUtil.getProjectPath() + "/" + target));
            } catch (IOException e) {
                Log.i("Reader",String.format("Не скопировалось в %s",  target));
                e.printStackTrace();
            }
        }
        return null;
    }
}
