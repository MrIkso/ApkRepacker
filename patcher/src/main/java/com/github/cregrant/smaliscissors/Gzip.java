package com.github.cregrant.smaliscissors;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip implements Serializable {
    private byte[] theCompressedArray;

    public Gzip(String s) {
        compress(s);
    }

    public void compress(String string) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream zos;
        try {
            zos = new GZIPOutputStream(bos);
            ObjectOutputStream ous = new ObjectOutputStream(zos);
            ous.writeObject(string);
            zos.finish();
            bos.flush();
            theCompressedArray = bos.toByteArray();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String decompress() {
        ByteArrayInputStream bis = new ByteArrayInputStream(theCompressedArray);
        GZIPInputStream zis;
        String tmpObject = null;
        try {
            zis = new GZIPInputStream(bis);
            ObjectInputStream ois = new ObjectInputStream(zis);
            tmpObject = (String) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return tmpObject;
    }
}