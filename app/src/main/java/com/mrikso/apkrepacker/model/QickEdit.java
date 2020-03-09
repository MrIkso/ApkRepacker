package com.mrikso.apkrepacker.model;

import android.os.Environment;

import com.mrikso.apkrepacker.utils.QickEditParams;
import com.mrikso.apkrepacker.utils.ZipUtils;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class QickEdit {
    /*
     * Главный класс в котором и происходит магия клонирования и прочего
     */
    private File source;
    private ZipFile zipFile;

    private String androidmanifest = "AndroidManifest.xml";

    public void build(File from, File to){
        source = from;
        try {
            zipFile = new ZipFile(source);
            patchManifest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void patchManifest(){
        ZipEntry entry = ZipUtils.getEntry(zipFile, androidmanifest);
        if(entry == null){
            return;
        }
        else {
            try {
                InputStream in = zipFile.getInputStream(entry);
             //   in.close();

             //   in.close();
                IOUtils.copy(in, new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/orig.xml", false));
                //in.close();
                ManifestEditor manifestEditor = new ManifestEditor(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/orig.xml"));
                manifestEditor.setAppName(QickEditParams.getNewname());
                manifestEditor.setPackageName(QickEditParams.getNewPackage());
                manifestEditor.setVersionName(QickEditParams.getVersionName());
                manifestEditor.setVersionCode(Integer.parseInt(QickEditParams.getVersionCode()));
                manifestEditor.setMinimumSdk(QickEditParams.getMinimumSdk());
                manifestEditor.setTargetSdk(QickEditParams.getTargetSdk());
                manifestEditor.commit();
                manifestEditor.writeTo(new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.xml", false));

                //in.close();
                // byte[] manifest = Util.readFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/orig.xml"));
               // AxmlReader axmlReader = new AxmlReader(manifest);
               // AxmlWriter axmlWriter = new AxmlWriter();
               // axmlReader.accept(new MyAxmlVisitor(axmlWriter));

               // byte[]mod = axmlWriter.toByteArray();
               // axmlReader.accept(new DumpAdapter(axmlWriter));
               // Util.writeFile(mod,new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.xml") );
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static byte[] processDex(DexBackedDexFile dex) throws Exception {
        DexBuilder dexBuilder = new DexBuilder(Opcodes.getDefault());

        MemoryDataStore store = new MemoryDataStore();
        dexBuilder.writeTo(store);
        return store.getData();
    }


}
