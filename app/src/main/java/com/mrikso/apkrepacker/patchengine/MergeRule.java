package com.mrikso.apkrepacker.patchengine;

import android.util.SparseIntArray;

import org.apache.commons.io.IOUtils;

import java.io.RandomAccessFile;
import java.util.List;
import java.util.zip.ZipFile;

public class MergeRule extends Core {
    private String source;
    public SparseIntArray c;
    
    MergeRule(){
        
    }
    
    @Override
    public void start(LineReader lineReader) {
        line = lineReader.getLine();
        String readLine = lineReader.readLine();
        while (readLine != null) {
            String trim = readLine.trim();
            if ("[/MERGE]".equals(trim)) {
                return;
            }
            if (super.checkName(trim, lineReader)) {
                readLine = lineReader.readLine();
            } else {
                if ("SOURCE:".equals(trim)) {
                    source = lineReader.readLine().trim();
                } else {
                    //bVar.a((int) R.string.patch_error_cannot_parse, Integer.valueOf(cVar.a()), trim);
                }
                readLine = lineReader.readLine();
            }
        }

    }
    @Override
    public boolean inSmali() {
        return true;
    }
    @Override
    public String currentRule(ZipFile zipFile) {
        return null;
    }


    public void mergeSmali(String str, List list) {
        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(str, "rw");
            try {
                long length = randomAccessFile.length();
                if (length < 16) {
                    throw new Exception("File is too small!");
                }
                randomAccessFile.seek(length - 16);
                byte[] bArr = new byte[32];
                int read = randomAccessFile.read(bArr);
                int i = 0;
                while (i < read && (bArr[i] != 60 || bArr[i + 1] != 47)) {
                    i++;
                }
                randomAccessFile.seek(((long) i) + (length - 16));
                StringBuilder sb = new StringBuilder();
                for (int i2 = 0; i2 < list.size(); i2++) {
                    sb.append((String) list.get(i2));
                    sb.append(IOUtils.LINE_SEPARATOR_UNIX);
                }
                sb.append("</resources>");
                randomAccessFile.write(sb.toString().getBytes());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
