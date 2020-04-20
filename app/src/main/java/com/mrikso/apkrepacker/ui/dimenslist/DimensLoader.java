package com.mrikso.apkrepacker.ui.dimenslist;

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

public class DimensLoader {
    private static final String TAG = "DimensLoader";

    private static DimensLoader sInstance;

    private Context mContext;
    private Executor mExecutor = Executors.newSingleThreadExecutor();

    private MutableLiveData<List<DimensMeta>> mDimensLiveData = new MutableLiveData<>();

    public DimensLoader(Context c, File file) {
        sInstance = this;

        mContext = c.getApplicationContext();
        mDimensLiveData.setValue(new ArrayList<>());
        fetchPackages(file);
    }

    public static DimensLoader getInstance(Context c, File file) {
        synchronized (DimensLoader.class) {
            return sInstance != null ? sInstance : new DimensLoader(c, file);
        }
    }

    public LiveData<List<DimensMeta>> getPackages() {
        return mDimensLiveData;
    }

    private void fetchPackages(File dimens) {
        mExecutor.execute(() -> {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            List<DimensMeta> dimensList = new ArrayList<>();
            long start = System.currentTimeMillis();
            try {
                DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
                Document doc = documentBuilder.parse(dimens);
                doc.getDocumentElement().normalize();
                NodeList nodeList = doc.getElementsByTagName("dimen");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String name = element.getAttribute("name");
                        String value = element.getTextContent();
                        DimensMeta dimensMeta = new DimensMeta.Builder(name)
                                .setValue(value)
//                                .setIcon(value)
                                .build();
                        dimensList.add(dimensMeta);
                    }
                }
            } catch (IOException | SAXException | ParserConfigurationException e) {
                e.printStackTrace();
            }

            //Collections.sort(colorsList, (p1, p2) -> p1.label.compareToIgnoreCase(p2.label));
            Log.d(TAG, String.format("Loaded dimens in %d ms", (System.currentTimeMillis() - start)));
            mDimensLiveData.postValue(dimensList);
        });
    }

}

