package com.mrikso.apkrepacker.ui.dimenslist;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DimensViewModel extends AndroidViewModel {

    private Context mContext;

    private DimensLoader mDimensRepo;
    private Observer<List<DimensMeta>> mBackupRepoPackagesObserver;

    private FilterQuery mCurrentFilterQuery = new FilterQuery("");
    private Filter mFilter = new DimensFilter();
    private List<DimensMeta> mRawDimens = new ArrayList<>();
    private List<DimensMeta> mUpdatedDimens = new ArrayList<>();
    private File dimensFile;
    private boolean isChanget = false;

    private MutableLiveData<List<DimensMeta>> mPackagesLiveData = new MutableLiveData<>();

    public DimensViewModel(@NonNull Application application) {
        super(application);
        mContext = application;
    }

    public LiveData<List<DimensMeta>> getDimens() {
        return mPackagesLiveData;
    }

    public void setDimensFile(File file) {
        dimensFile = file;
        mPackagesLiveData.setValue(new ArrayList<>());

        mDimensRepo = new DimensLoader(mContext, dimensFile);
        mBackupRepoPackagesObserver = (packages) -> filter(mCurrentFilterQuery);
        mDimensRepo.getPackages().observeForever(mBackupRepoPackagesObserver);
        Log.i("DimensViewModel", String.format("dimens file %s", file.getAbsolutePath()));
    }

    public void setNewDimens(int position, String dimens, String name) {
        mUpdatedDimens = mDimensRepo.getPackages().getValue();
        DimensMeta dimenMeta = new DimensMeta.Builder(name)
                .setValue(dimens)
//                .setIcon(dimens)
                .build();
        mUpdatedDimens.set(position, dimenMeta);
        isChanget =true;
        saveDimens();
    }

    public void filter(String query) {
        filter(new FilterQuery(query));
    }

    private void filter(FilterQuery filterQuery) {
        mCurrentFilterQuery = filterQuery;
        //TODO probably do something about this cuz concurrency. Tho it should still be fine
        mRawDimens = mDimensRepo.getPackages().getValue();
        mFilter.filter(filterQuery.serializeToString());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mDimensRepo.getPackages().removeObserver(mBackupRepoPackagesObserver);
    }

    private static class FilterQuery {
        String query;

        FilterQuery(String query) {
            this.query = query;
        }

        static FilterQuery fromString(String serializedFilterQuery) {
            return new FilterQuery(serializedFilterQuery);
        }

        String serializeToString() {
            return query;
        }
    }

    public void saveDimens(){
        int tempnumber = 0; // Temp number for (<string name="...">)
       // File resultFile = new File(projectPatch+"/res/values-"+language+"/");
       // resultFile.mkdirs(); // Create path
        DocumentBuilderFactory dbFactory;
        DocumentBuilder dBuilder;
        Document doc;
        try {
        dbFactory = DocumentBuilderFactory.newInstance();
        dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.newDocument();
            // root element
            Element rootElement = doc.createElement("resources"); // Create resources in document
            doc.appendChild(rootElement); // Add resources in document

            // string element
            List<DimensMeta> dimens = new ArrayList<>(mUpdatedDimens);
            for (DimensMeta dimensMeta : dimens) {
                String dimensId = dimensMeta.label;
                String dimensText = dimensMeta.value;

                //String temp = translatorfunction(mo, lenguage);

                Element stringelement = doc.createElement("dimen"); // Create string in document
                Attr attrType = doc.createAttribute("name"); // Create atribute name
                attrType.setValue(dimensId); // Add to "name" the word code
                stringelement.setAttributeNode(attrType); // Add atribute to string elemt
                stringelement.appendChild(doc.createTextNode(dimensText)); // Add translated word to string
                rootElement.appendChild(stringelement); // Add string element to document

                tempnumber++;
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            //    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            DOMSource source = new DOMSource(doc);
            FileOutputStream fileOutputStream = new FileOutputStream(dimensFile); // Write file
            transformer.transform(source, new StreamResult(fileOutputStream));

            // Output to console for testing
            // StreamResult consoleResult = new StreamResult(System.out);
            //transformer.transform(source, consoleResult);

        } catch (Exception e) {
            //Toast.makeText(App.getContext(), getResources().getString(R.string.toast_error_translate_language), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    private class DimensFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<DimensMeta> dimens = new ArrayList<>(isChanget ? mUpdatedDimens : mRawDimens);
            FilterQuery filterQuery = FilterQuery.fromString(constraint.toString());
            String query = filterQuery.query.toLowerCase();

            Iterator<DimensMeta> iterator = dimens.iterator();
            while (iterator.hasNext()) {
                DimensMeta packageMeta = iterator.next();
                //Apply query
                if (query.length() > 0) {
                    //Check if app label matches
                    String[] wordsInLabel = packageMeta.label.toLowerCase().split(" ");
                    boolean labelMatches = false;
                    for (String word : wordsInLabel) {
                        if (word.contains(query)) {
                            labelMatches = true;
                            break;
                        }
                    }

                    //Check if app packages matches
                    boolean packagesMatches = packageMeta.value.toLowerCase().contains(query);

                    if (!labelMatches && !packagesMatches)
                        iterator.remove();
                }
            }

            FilterResults results = new FilterResults();
            results.values = new ArrayList<>(dimens);
            results.count = dimens.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mPackagesLiveData.setValue((List<DimensMeta>) results.values);
        }
    }
}

