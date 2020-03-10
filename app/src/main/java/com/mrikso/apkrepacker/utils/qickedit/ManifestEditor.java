package com.mrikso.apkrepacker.utils.qickedit;

import com.mrikso.apkrepacker.utils.QickEditParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.AxmlWriter;
import pxb.android.axml.NodeVisitor;
import pxb.android.axml.Util;

public class ManifestEditor {

    private final String[] components = {"activity", "activity-alias", "provider", "receiver", "service"};
    private File mManifest;
    private int mVersionCode = -1;
    private int mMinimumSdk = -1;
    private int mTargetSdk = -1;
    private int mInstallLocation = -1;
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

    public ManifestEditor setInstallLocation(int location) {
        mInstallLocation = location;
        return this;
    }

    public ManifestEditor commit() throws IOException {
        AxmlReader reader = new AxmlReader(Util.readFile(mManifest));
        AxmlWriter writer = new AxmlWriter();
        reader.accept(new AxmlVisitor(writer) {
            public NodeVisitor child(String ns, String name)//manifest
            {
                return new NodeVisitor(super.child(ns, name)) {
                    public NodeVisitor child(String ns, String name) //manifest's child nodes
                    {
                        if (name.equalsIgnoreCase("uses-sdk")) {
                            return new NodeVisitor(super.child(ns, name)) {

                                @Override
                                public void attr(String ns, String name, int resourceId, int type, Object value) {
                                    if (name.equalsIgnoreCase("minSdkVersion") && mMinimumSdk > 0) {
                                        value = mMinimumSdk;
                                        type = TYPE_FIRST_INT;
                                    } else if (name.equalsIgnoreCase("targetSdkVersion") && mTargetSdk > 0) {
                                        value = mTargetSdk;
                                        type = TYPE_FIRST_INT;
                                    }
                                    super.attr(ns, name, resourceId, type, value);
                                }
                            };
                        }
                        else if (name.equalsIgnoreCase("application")) {
                            return new NodeVisitor(super.child(ns, name)) {
                                public NodeVisitor child(String ns, String name) {
                                    if (mPackageName == null || QickEditParams.getOldPackage() == null) {
                                        return super.child(ns, name);
                                    }
                                    for (String component : components) {
                                        if (name.equalsIgnoreCase(component)) {
                                            return new NodeVisitor(super.child(ns, name)) {

                                                @Override
                                                public void attr(String ns, String name, int resourceId, int type, Object value) {
                                                    if (name.equalsIgnoreCase("name") && value instanceof String) {
                                                        int check = ((String) value).indexOf(".");
                                                        if (check < 0) {
                                                            value = mPackageName + "." + value;
                                                        } else if (check == 0) {
                                                            value = mPackageName + value;
                                                        }
                                                        type = TYPE_STRING;
                                                    }
                                                    super.attr(ns, name, resourceId, type, value);
                                                }
                                            };
                                        }
                                    }
                                    return super.child(ns, name);
                                }

                                @Override
                                public void attr(String ns, String name, int resourceId, int type, Object value) {
                                    if (name.equalsIgnoreCase("label") && mAppName != null) {
                                        value = mAppName;
                                        type = TYPE_STRING;
                                    } else if (name.equalsIgnoreCase("extractNativeLibs")) {
                                        return;
                                    }
                                    super.attr(ns, name, resourceId, type, value);
                                }
                            };
                        }
                        return super.child(ns, name);
                    }

                    @Override
                    public void attr(String ns, String name, int resourceId, int type, Object value) {
                        if (name.equalsIgnoreCase("package") && mPackageName != null) {
                            value = mPackageName;
                            type = TYPE_STRING;
                        }else if(name.equalsIgnoreCase("installLocation")){
                            int loc = getRealInstallLocation(mInstallLocation);
                            if(loc >= 0){
                                value = loc;
                                type = TYPE_FIRST_INT;
                            }
                            else {
                                return;
                            }

                        }
                        else if (name.equalsIgnoreCase("versionName") && mVersionName != null) {
                            value = mVersionName;
                            type = TYPE_STRING;
                        } else if (name.equalsIgnoreCase("versionCode") && mVersionCode > 0) {
                            value = mVersionCode;
                            type = TYPE_FIRST_INT;
                        }
                        super.attr(ns, name, resourceId, type, value);
                    }
                };
            }
        });
        mManifestData = writer.toByteArray();
        return this;
    }


    public void writeTo(FileOutputStream manifestOutputStream) throws IOException {
        manifestOutputStream.write(mManifestData);
        manifestOutputStream.close();
    }

    /*
        Return real install location from selected item in spinner
     */
    private int getRealInstallLocation(int installLocation) {
        switch (installLocation) {
            case 0:
                return -1;//default
            case 1:
                return 0;//auto
            case 2:
                return 1;//internal
            case 3:
                return 2;//external
            default:
                return -1;
        }
    }

}
