package com.mrikso.apkrepacker.ui.colorslist;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ColorsLoader {
    private static final String TAG = "ColorsLoader";

    private static ColorsLoader sInstance;

    private Context mContext;
    private Executor mExecutor = Executors.newSingleThreadExecutor();

    private MutableLiveData<List<ColorMeta>> mColorsLiveData = new MutableLiveData<>();

    private ColorsLoader(Context c, File file) {
        sInstance = this;

        mContext = c.getApplicationContext();
        mColorsLiveData.setValue(new ArrayList<>());
        fetchPackages(file);
    }

    public static ColorsLoader getInstance(Context c, File file) {
        synchronized (ColorsLoader.class) {
            return sInstance != null ? sInstance : new ColorsLoader(c, file);
        }
    }

    public LiveData<List<ColorMeta>> getPackages() {
        return mColorsLiveData;
    }

    private void fetchPackages(File colors) {
        mExecutor.execute(() -> {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            List<ColorMeta> colorsList = new ArrayList<>();
            long start = System.currentTimeMillis();
            try {
                DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
                Document doc = documentBuilder.parse(colors);
                doc.getDocumentElement().normalize();
                NodeList nodeList = doc.getElementsByTagName("color");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String name = element.getAttribute("name");
                        String value = element.getTextContent();
                        ColorMeta colorMeta = new ColorMeta.Builder(name)
                                .setValue(value)
                                .setIcon(value)
                                .build();
                        colorsList.add(colorMeta);
                    }
                }
            } catch (IOException | SAXException | ParserConfigurationException e) {
                e.printStackTrace();
            }

            //Collections.sort(colorsList, (p1, p2) -> p1.label.compareToIgnoreCase(p2.label));
            Log.d(TAG, String.format("Loaded colors in %d ms", (System.currentTimeMillis() - start)));
            mColorsLiveData.postValue(colorsList);
        });
    }

}

