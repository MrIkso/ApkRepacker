package com.github.cregrant.smaliscissors.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Prefs {
    public static String run_type = "";
    public static String projectPath = "";
    public static File patchesDir;
    public static File tempDir;
    private static double versionConf = 0.01;
    static int verbose_level = 1;
    static boolean keepSmaliFilesInRAM = false;
    static boolean keepXmlFilesInRAM = false;
    static boolean skipSomeSmaliFiles = true;
    static String[] smaliFoldersToSkip = new String[]{"android", "androidx", "kotlin", "kotlinx"};
    //todo move to main

    public void loadConf() {
        Properties props = new Properties();
        String settingsFilename = System.getProperty("user.dir") + File.separator + "config" + File.separator + "conf.txt";
        try {
            FileInputStream input = new FileInputStream(settingsFilename);
            props.load(input);
            input.close();
        }
        catch (Exception e) {
            Main.out.println("Error loading conf!");
        }
        try {
            if (props.size() == 0) {
                saveConf();
                Main.out.println("Config file broken or unreachable. Using default one.");
            }
            verbose_level = Integer.parseInt(props.getProperty("Verbose_level"));
            versionConf = Float.parseFloat(props.getProperty("Version"));
            keepSmaliFilesInRAM = Boolean.parseBoolean(props.getProperty("Keep_smali_files_in_RAM"));
            keepXmlFilesInRAM = Boolean.parseBoolean(props.getProperty("Keep_xml_files_in_RAM"));
        }
        catch (Exception e) {
            Main.out.println("Error reading conf!");
        }
        if (Main.version - versionConf > 0.001) {
            new Prefs().upgradeConf();
        }
    }

    void saveConf() {
        try {
            FileOutputStream output = new FileOutputStream(System.getProperty("user.dir") + File.separator + "config" + File.separator + "conf.txt");
            Properties props = new Properties();
            props.put("Version", String.format("%.2f", versionConf).replace(',', '.'));
            props.put("Verbose_level", String.valueOf(verbose_level));
            props.put("Keep_smali_files_in_RAM", ((Boolean) keepSmaliFilesInRAM).toString());
            props.put("Keep_xml_files_in_RAM", ((Boolean) keepXmlFilesInRAM).toString());
            props.store(output, "");
            output.close();
        }
        catch (Exception e) {
            Main.out.println("Error writing conf: " + e.getMessage());
        }
    }

    private void upgradeConf() {
        Main.out.println("Upgrading config file...");
        Main.out.println(versionConf + " --> 0.01");
        versionConf = 0.01f;
        this.saveConf();
        Main.out.println("Upgraded.");
    }
}