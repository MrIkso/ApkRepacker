package com.mrikso.apkrepacker.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.android.apksig.ApkSigner;
import com.google.common.collect.ImmutableList;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.prererence.PreferenceHelper;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.logging.Level;

import brut.util.Logger;
import sun1.security.pkcs.PKCS8Key;

public class SignUtil {
    private static PreferenceHelper preferenceHelper;

    public static void loadKey(Context context, LoadKeyCallback callback)
    {
        preferenceHelper = PreferenceHelper.getInstance(context);
        msgId = 0;
        msgObj = null;
        boolean custom = preferenceHelper.isCustomSign();
        if (custom)
        {
            int type = preferenceHelper.getKeyType();
            String keyPath = preferenceHelper.getPrivateKeyPath();
            String cert_or_alias = preferenceHelper.getCertPath();
            String store_pass = preferenceHelper.getStoreKey();
            String key_pass = preferenceHelper.getPrivateKey();
            try
            {
                if (type == 3)
                    custom = loadKey(callback, keyPath, cert_or_alias);
                else
                    custom = loadKey(context, callback, keyPath, type, cert_or_alias, store_pass, key_pass);
            }
            catch (Exception e)
            {
                error(context, keyPath);
            }
        }
        if (!custom)
        {
            try {
                SignUtil st = new SignUtil();
                InputStream cert = new FileInputStream(preferenceHelper.getCertPath());
                InputStream key = new FileInputStream(preferenceHelper.getPrivateKeyPath());
                PKCS8Key pkcs8 = new PKCS8Key();
                pkcs8.decode(key);

                st.privateKey = pkcs8 ;
                st.certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(cert);
                cert.close();
                key.close();
                callback.call(st);
            } catch (InvalidKeyException | CertificateException | FileNotFoundException  e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void error(Context context, String keyPath)
    {
        String msg = context.getResources().getString(R.string.load_signature_file_fail, keyPath);
        new AlertDialog.Builder(context).
                setTitle(R.string.error).
                setMessage(msg).
                setPositiveButton(R.string.ok, null).
                show();
    }

    private static final String[] types = {"JKS", "PKCS12", "BKS"};

    private static boolean loadKey(Context context, SignUtil.LoadKeyCallback callback, String keyPath, int type, String alias, String store_pass, String key_pass) throws Exception
    {
        if (!exists(keyPath))
            return false;
        String keyType = types[type];
        final KeyStore ks = KeyStore.getInstance(keyType);
        if (store_pass.isEmpty())
        {
            showPasswd(context, callback, ks, keyPath, alias);
        }
        else
        {
            char[] storePass = store_pass.toCharArray();
            char[] keyPass;
            if (key_pass.isEmpty())
                keyPass = storePass;
            else
                keyPass = key_pass.toCharArray();
            loadKey(callback, ks, keyPath, alias, storePass, keyPass);
        }
        return true;
    }

    private static void showPasswd(final Context context, final SignUtil.LoadKeyCallback callback, final KeyStore ks, final String keyPath, final String alias)
    {
        View view = LayoutInflater.from(context).
                inflate(R.layout.dialog_key_password, null);
        final EditText storePass = view.findViewById(R.id.storePass);
        final EditText keyPass = view.findViewById(R.id.keyPass);
        TextView msg = view.findViewById(R.id.msg);
        msg.setVisibility(View.VISIBLE);
        msg.setText(keyPath);
        new AlertDialog.Builder(context).
                setTitle(R.string.enter_password).
                setView(view).
                setPositiveButton(android.R.string.ok, (p1, p2) -> {
                    String store_pass = storePass.getText().toString();
                    String key_pass = keyPass.getText().toString();
                    if (key_pass.isEmpty())
                        key_pass = store_pass;
                    try
                    {
                        loadKey(callback, ks, keyPath, alias, store_pass.toCharArray(), key_pass.toCharArray());
                    }
                    catch (Exception e)
                    {
                        error(context, keyPath);
                    }
                }).
                setNegativeButton(android.R.string.cancel, null).
                show();
    }

    private static void loadKey(SignUtil.LoadKeyCallback callback, KeyStore ks, String keyPath, String alias, char[] storePass, char[] keyPass) throws Exception
    {
        InputStream i = new FileInputStream(keyPath);
        ks.load(i, storePass);
        if (alias.isEmpty())
            alias = ks.aliases().nextElement();
        PrivateKey prk = (PrivateKey) ks.getKey(alias, keyPass);
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        SignUtil st = new SignUtil();
        st.privateKey = prk;
        st.certificate = cert;
        callback.call(st);
    }

    private static boolean loadKey(SignUtil.LoadKeyCallback callback, String keyPath, String certPath) throws Exception
    {
        if (!exists(keyPath))
            return false;
        if (!exists(certPath))
            return false;
        InputStream pk = new FileInputStream(keyPath);
        byte[] data = IOUtils.toByteArray(pk);
        pk.close();
        InputStream cer = new FileInputStream(certPath);
        X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").
                generateCertificate(cer);
        cer.close();
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(data);
        PrivateKey prk = KeyFactory.getInstance(cert.getPublicKey().getAlgorithm()).generatePrivate(spec);
        SignUtil st = new SignUtil();
        st.privateKey = prk;
        st.certificate = cert;
        callback.call(st);
        return true;
    }

    private static boolean exists(String path)
    {
        if (new File(path).exists())
            return true;
        msgId = R.string.signature_file_missing;
        msgObj = path;
        return false;
    }

    private static int msgId;
    private static Object msgObj;
    private PrivateKey privateKey;
    private X509Certificate certificate;

    private SignUtil()
    {}

    public boolean sign(File in, File out, int minSdk, Logger logger) throws Exception {
        if (msgId > 0)
            logger.warning(msgId, msgObj);
        if (out.exists()) {
            FileUtil.deleteFile(out);
        }
        ApkSigner.SignerConfig.Builder builder = new ApkSigner.SignerConfig.Builder("CERT", privateKey, ImmutableList.of(certificate));
        ApkSigner.SignerConfig signerConfig = builder.build();
        ApkSigner.Builder apksigner = new ApkSigner.Builder(ImmutableList.of(signerConfig));
        apksigner.setInputApk(in);
        apksigner.setOutputApk(out);
        apksigner.setCreatedBy("ApkRepacker by Mr Ikso");
        apksigner.setMinSdkVersion(minSdk);
        apksigner.setV1SigningEnabled(true);
        apksigner.setV2SigningEnabled(preferenceHelper.isV2SignatureEnabled());
        ApkSigner signer = apksigner.build();
        logger.info(R.string.log_text, String.format("Signing Apk: %s", in));
        try
        {
            signer.sign();
            logger.info(R.string.sign_done, out);
            return true;
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING, "Signature failed! ", e);
            e.printStackTrace();
            return false;
        }
    }

    public boolean sign(File in, File out, int minSdk) throws Exception {

            //logger.warning(msgId, msgObj);
        if (out.exists()) {
            FileUtil.deleteFile(out);
        }
        ApkSigner.SignerConfig.Builder builder = new ApkSigner.SignerConfig.Builder("CERT", privateKey, ImmutableList.of(certificate));
        ApkSigner.SignerConfig signerConfig = builder.build();
        ApkSigner.Builder apksigner = new ApkSigner.Builder(ImmutableList.of(signerConfig));
        apksigner.setInputApk(in);
        apksigner.setOutputApk(out);
        apksigner.setCreatedBy("Apk Repacker by Mr Ikso");
        apksigner.setMinSdkVersion(minSdk);
        apksigner.setV1SigningEnabled(true);
        apksigner.setV2SigningEnabled(preferenceHelper.isV2SignatureEnabled());
        ApkSigner signer = apksigner.build();
        //logger.info(R.string.log_text, String.format("Signing Apk: %s", in));
        try
        {
            signer.sign();
            //UIUtils.toast(App.getContext(), R.string.toast_sign_done);
            return true;
        }
        catch (Exception e)
        {
           // UIUtils.toast(App.getContext(), R.string.toast_error_sign_failed);
          //  logger.log(Level.WARNING, "Signature failed! ", e);
            e.printStackTrace();
            return false;
        }
    }

    public interface LoadKeyCallback
    {
        void call(SignUtil signTool);
    }

    static {
        try
        {
            sha256 = MessageDigest.getInstance("SHA-256");
            sha1 = MessageDigest.getInstance("SHA-1");
            md5 = MessageDigest.getInstance("MD5");
        }
        catch (Exception ignored)
        {
        }
    }

    private static MessageDigest md5;
    private static MessageDigest sha1;
    private static MessageDigest sha256;
}
