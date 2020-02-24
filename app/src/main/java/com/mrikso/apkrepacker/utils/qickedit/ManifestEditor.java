package com.mrikso.apkrepacker.utils.qickedit;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import pxb.android.StringItem;
import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlWriter;
import pxb.android.axml.NodeVisitor;
import pxb.android.axml.Util;

/**
 * Created by Stardust on 2017/10/23.
 */

public class ManifestEditor {

    private static final String NS_ANDROID = "http://schemas.android.com/apk/res/android";
    private File mManifest;
    private int mVersionCode = -1;
    private int mMinimumSdk = -1;
    private int mTargetSdk = -1;
    private String mVersionName;
    private String mAppName;
    private String mPackageName;
    private byte[] mManifestData;


    public ManifestEditor(File manifestInputStream) {
        mManifest = manifestInputStream;
    }

    public void setVersionCode(int versionCode) {
        mVersionCode = versionCode;
    }

    public ManifestEditor setVersionName(String versionName) {
        mVersionName = versionName;
        return this;
    }

    public ManifestEditor setAppName(String appName) {
        mAppName = appName;
        return this;
    }

    public ManifestEditor setPackageName(String packageName) {
        mPackageName = packageName;
        return this;
    }
    public ManifestEditor setMinimumSdk(int sdk) {
        mMinimumSdk = sdk;
        return this;
    }
    public ManifestEditor setTargetSdk(int sdk) {
        mTargetSdk = sdk;
        return this;
    }

    public ManifestEditor commit() throws IOException {
        AxmlWriter writer = new MutableAxmlWriter();
        //byte[]content = new byte[mManifestInputStream.available()];
        AxmlReader reader = new AxmlReader(Util.readFile(mManifest));
        reader.accept(writer);
        mManifestData = writer.toByteArray();
        return this;
    }


    public void writeTo(FileOutputStream manifestOutputStream) throws IOException {
        manifestOutputStream.write(mManifestData);
        manifestOutputStream.close();
    }

    private void onAttr(AxmlWriter.Attr attr) {
        if ("package".equalsIgnoreCase(attr.name.data) && mPackageName != null && attr.value instanceof StringItem) {
            ((StringItem) attr.value).data = mPackageName;
            return;
        }
        if (attr.ns == null || !NS_ANDROID.equals(attr.ns.data)) {
            return;
        }
        if ("versionCode".equalsIgnoreCase(attr.name.data) && mVersionCode != -1) {
            attr.value = mVersionCode;
            return;
        }
        if ("versionName".equalsIgnoreCase(attr.name.data) && mVersionName != null && attr.value instanceof StringItem) {
            //attr.value = new StringItem(mVersionName);
            Log.i("ManifestEditor", attr.value + " new " + mVersionName);
            ((StringItem) attr.value).data = mVersionName;
            return;
        }
        if ("minSdkVersion".equalsIgnoreCase(attr.name.data) && mMinimumSdk != -1) {
            attr.value = mMinimumSdk;
            return;
        }
        if ("targetSdkVersion".equalsIgnoreCase(attr.name.data) && mTargetSdk != -1) {
            attr.value = mTargetSdk;
            return;
        }
        int i3;

        if ("label".equalsIgnoreCase(attr.name.data) && mAppName != null) {
            Log.i("ManifestEditor", attr.value + " new " + mAppName);
            ((StringItem) attr.value).data = mVersionName;
            //i3 = 3;
            return;
        }
    }


    private class MutableAxmlWriter extends AxmlWriter {
        private class MutableNodeImpl extends AxmlWriter.NodeImpl {

            MutableNodeImpl(String ns, String name) {
                super(ns, name);
            }

            @Override
            protected void onAttr(AxmlWriter.Attr a) {
                ManifestEditor.this.onAttr(a);
                super.onAttr(a);
            }


            @Override
            public NodeVisitor child(String ns, String name) {
                NodeImpl child = new MutableNodeImpl(ns, name);
                this.children.add(child);
                return child;
            }

        }

        @Override
        public NodeVisitor child(String ns, String name) {
            NodeImpl first = new MutableNodeImpl(ns, name);
            this.firsts.add(first);
            return first;
        }
    }

}
