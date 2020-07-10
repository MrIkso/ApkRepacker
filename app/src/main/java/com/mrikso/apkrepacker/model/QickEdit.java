package com.mrikso.apkrepacker.model;

import android.graphics.Bitmap;

import com.mrikso.apkrepacker.utils.common.DLog;
import com.mrikso.apkrepacker.utils.QickEditParams;
import com.mrikso.apkrepacker.utils.qickedit.IconGenerate;
import com.mrikso.apkrepacker.utils.qickedit.ManifestEditor;

import org.apache.commons.io.IOUtils;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.MemoryDataStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class QickEdit {
    /*
     * Главный класс в котором и происходит магия клонирования и прочего
     */
    private final String[] ignoreStarts = new String[]{"assets/ugc", "assets/yandexnavi/fonts/tiles", "res/raw/netdisk", "assets/yandexnavi/fonts/", "res/raw/langid.data", "res/raw/joda", "assets/adp", "assets/js-modules/UNBUNDLE", "res/raw/estool", "res/raw/feature", "res/raw/irlocaldata", "assets/sound-strings/", "res/raw/selection", "res/raw/sb", "res/raw/guides", "res/raw/metadata.json", "res/raw/sm", "assets/cuisine-strings/", "res/raw/fill", "res/raw/transform", "assets/metadata.json", "res/raw/copic", "res/raw/layers", "res/raw/dav", "res/raw/test", "res/raw/timelapse", "res/raw/pulsar", "res/raw/cuscs", "res/raw/gtm", "res/raw/megviifacepp", "assets/countries-strings/", "assets/services/", "res/raw/ep", "assets/ABBYY.license", "res/raw/bnbp", "libs/", "res/raw/tw", "res/raw/bear.tiff", "res/raw/yvideo", "res/raw/spki"};
    private final String[] ignoreEnds = new String[]{".jpg", ".jpeg", ".png", ".gif", ".wav", ".mp2", ".mp3", ".ogg", ".aac", ".mpg", ".mpeg", ".mid", ".midi", ".smf", ".jet", ".rtttl", ".imy", ".xmf", ".mp4", ".m4a", ".m4v", ".3gp", ".3gpp", ".3g2", ".3gpp2", ".amr", ".awb", ".wma", ".wmv", ".avc", ".der", ".pfx", ".kml", ".pic", ".bc", ".key", ".glsl", ".plugin", ".p12", ".dat", ".cer", ".pb", ".bks", ".woff2", ".res", "/thumbnail", ".binarypb", ".bin", ".dict", ".zip", ".pk8", ".mov", ".crt"};
    private File source;
    private ZipFile zipFile;
    private Bitmap bitmap;
    private String iconName;

    private String androidmanifest = "AndroidManifest.xml";

    public void build(File from, File to){
        source = from;
        try {
            zipFile = new ZipFile(source);
            String type = null;
            String name = null;
            bitmap = QickEditParams.getBitmap();
            iconName = QickEditParams.getIconName();
            if (bitmap != null && iconName != null)
            {
                String[] tn = iconName.split("/");
                type = tn[0];
                name = tn[1];
                File tmp = File.createTempFile("temp", null);
                DLog.i("Generate new icon");
                IconGenerate.generate(tmp.getParent(), bitmap, name);
            }
            DLog.i("repack apk");
            repackApk(from,to, type, name);
           // patchManifest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File patchManifest(ZipEntry manifest){
    //    ZipEntry entry = ZipUtils.getEntry(zipFile, androidmanifest);
        if(manifest == null){
            return null;
        }
        else {
            try {
                InputStream in = zipFile.getInputStream(manifest);
                File originalManifest = File.createTempFile("orig", "xml");
                File editedManifest = File.createTempFile("edited", "xml");
                IOUtils.copy(in, new FileOutputStream(originalManifest, false));
                ManifestEditor manifestEditor = new ManifestEditor(originalManifest);
                manifestEditor.setAppName(QickEditParams.getNewname());
                manifestEditor.setPackageName(QickEditParams.getNewPackage());
                manifestEditor.setVersionName(QickEditParams.getVersionName());
                manifestEditor.setVersionCode(Integer.parseInt(QickEditParams.getVersionCode()));
                manifestEditor.setMinimumSdk(QickEditParams.getMinimumSdk());
                manifestEditor.setTargetSdk(QickEditParams.getTargetSdk());
                manifestEditor.setInstallLocation(QickEditParams.getInstallLoacation());
                manifestEditor.commit();
                manifestEditor.writeTo(new FileOutputStream(editedManifest, false));
                return editedManifest;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    private static byte[] processDex(DexBackedDexFile dex) throws Exception {
        DexBuilder dexBuilder = new DexBuilder(Opcodes.getDefault());

        MemoryDataStore store = new MemoryDataStore();
        dexBuilder.writeTo(store);
        return store.getData();
    }

    private void repackApk(File srcApk, File targetApk, String type, String name) throws IOException
    {
        ZipInputStream zin = new ZipInputStream(new FileInputStream(srcApk));
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(targetApk));
        zout.setLevel(Deflater.BEST_COMPRESSION);
        ZipEntry entry;
        byte[] buffer = new byte[2048];
        File dir = targetApk.getParentFile();
        while ((entry = zin.getNextEntry()) != null)
        {
            String ename = entry.getName();
            //add new icon
            if (type != null && name != null && ename.startsWith("res/" + type) && ename.startsWith(name + ".png"))
            {
                int length;
                File ic = null;
                for (String dens : IconGenerate.mDens)
                {
                    if (ename.startsWith("res/" + type + "-" + dens))
                        ic = new File(dir.getAbsolutePath() + File.separator + dens + File.separator + name + ".png");
                }
                if (ic == null)
                    ic = new File(dir.getAbsolutePath() + File.separator + "xxxhdpi" + File.separator + name + ".png");
                FileInputStream fin = new FileInputStream(ic);
                ZipEntry outEntry = new ZipEntry(ename);
                outEntry.setCompressedSize(ic.length());
                outEntry.setMethod(ZipEntry.DEFLATED);
                zout.putNextEntry(outEntry);
                while ((length = fin.read(buffer, 0, buffer.length)) > 0)
                {
                    zout.write(buffer, 0, length);
                }
                zout.flush();
                zout.closeEntry();
            }
            //patch and add new icon
            else if (androidmanifest.equals(ename))
            {
                DLog.i("patch manifest");
                int length;

                File tempChangedFile = patchManifest(entry);

                FileInputStream fin = new FileInputStream(tempChangedFile);
                ZipEntry outEntry = new ZipEntry(ename);
                outEntry.setCompressedSize(tempChangedFile.length());
                outEntry.setMethod(ZipEntry.DEFLATED);
                zout.putNextEntry(outEntry);
                while ((length = fin.read(buffer, 0, buffer.length)) > 0)
                {
                    zout.write(buffer, 0, length);
                }
                zout.flush();
                zout.closeEntry();
            }
            else
            {
                ZipEntry outEntry = new ZipEntry(ename);
                long size = entry.getSize();
                long crc = entry.getCrc();
                if (isIgnore(ename) && crc >= 0 && size >= 0)
                {
                    outEntry.setMethod(ZipEntry.STORED);
                    outEntry.setSize(size);
                    outEntry.setCrc(crc);
                }
                else
                {
                    outEntry.setMethod(ZipEntry.DEFLATED);
                }
                outEntry.setCompressedSize(entry.getSize());
                zout.putNextEntry(outEntry);
                int length;
                while ((length = zin.read(buffer, 0, buffer.length)) > 0)
                {
                    zout.write(buffer, 0, length);
                }
                zout.flush();
                zout.closeEntry();
            }
            zin.closeEntry();
        }
        zin.close();
        zout.flush();
        zout.close();
    }
    private boolean isIgnore(String name)
    {
        if (name.toLowerCase().startsWith("r/"))
        {
            return true;
        }
        for (String start : ignoreStarts)
        {
            if (name.startsWith(start))
            {
                return true;
            }
        }
        if (name.startsWith("res/raw") || name.startsWith("assets/"))
        {
            for (String end : ignoreEnds)
            {
                if (name.endsWith(end))
                {
                    return true;
                }
            }
        }
        return false;
    }

}
