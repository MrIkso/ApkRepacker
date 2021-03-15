package com.github.cregrant.smaliscissors;

import java.io.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApkSigSpoof {
    public static void spoof(String projectPath, String apkPath) {
        //todo add PmsHookApplication.smali before call spoof()
        try {
            String smali = IO.read(projectPath + File.separator + "/smali/cc/binmt/signature/PmsHookApplication.smali");
            String[] ss = new String[1]; ss[0] = apkPath;
            byte[] cert = ApkSigSpoof.getEncodedSig(ss);
            if (cert==null)
                throw new Exception();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.write(1);
            dataOutputStream.writeInt(cert.length);
            dataOutputStream.write(cert);
            String replace = smali.replace("### Signatures Data ###", Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            FileOutputStream fileOutputStream = new FileOutputStream(projectPath + File.separator + "/smali/cc/binmt/signature/PmsHookApplication.smali");
            fileOutputStream.write(replace.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] getEncodedSig(String[] args) {
        String mArchiveSourcePath = args[0];
        try {
            JarFile jarFile = new JarFile(mArchiveSourcePath);
            Certificate[] certs = null;
            Enumeration<JarEntry> entries = jarFile.entries();

            while (true) {
                JarEntry je;
                do {
                    do {
                        if (!entries.hasMoreElements()) {
                            jarFile.close();

                            if (certs != null && certs.length > 0) {
                                for (Certificate cert : certs) {
                                    return cert.getEncoded();
                                }
                            }

                            System.err.println("Package has no certificates; ignoring!");
                            return null;
                        }

                        je = entries.nextElement();
                    } while (je.isDirectory());
                } while (je.getName().startsWith("META-INF/"));

                InputStream is = jarFile.getInputStream(je);
                is.readAllBytes();
                is.close();
                Certificate[] localCerts = je.getCertificates();
                if (localCerts == null) {
                    System.err.println("Package has no certificates at entry " + je.getName() + "; ignoring!");
                    jarFile.close();
                    return null;
                }

                if (certs != null) {
                    for (Certificate cert : certs) {
                        boolean found = false;

                        for (Certificate localCert : localCerts) {
                            if (cert != null && cert.equals(localCert)) {
                                found = true;
                                break;
                            }
                        }

                        if (!found || certs.length != localCerts.length) {
                            System.err.println("Package has mismatched certificates at entry " + je.getName() + "; ignoring!");
                            jarFile.close();
                            return null;
                        }
                    }
                } else {
                    certs = localCerts;
                }
            }
        } catch (CertificateEncodingException var14) {
            Logger.getLogger(ApkSigSpoof.class.getName()).log(Level.SEVERE, null, var14);
        } catch (IOException | RuntimeException var15) {
            System.err.println("Exception reading " + mArchiveSourcePath + "\n" + var15);
        }
        return null;
    }
}

