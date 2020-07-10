package com.mrikso.apkrepacker.ui.colorslist;

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

public class ColorsViewModel extends AndroidViewModel {

    private Context mContext;

    private ColorsLoader mColorRepo;
    private Observer<List<ColorMeta>> mBackupRepoPackagesObserver;

    private FilterQuery mCurrentFilterQuery = new FilterQuery("");
    private Filter mFilter = new ColorFilter();
    private List<ColorMeta> mRawColors = new ArrayList<>();
    private List<ColorMeta> mUpdatedColors = new ArrayList<>();
    private File colorsFile;
    private boolean isChanget = false;

    private MutableLiveData<List<ColorMeta>> mPackagesLiveData = new MutableLiveData<>();

    public ColorsViewModel(@NonNull Application application) {
        super(application);
        mContext = application;
    }

    public LiveData<List<ColorMeta>> getColors() {
        return mPackagesLiveData;
    }

    public void setColorsFile(File file) {
        colorsFile = file;
        mPackagesLiveData.setValue(new ArrayList<>());

        mColorRepo = new ColorsLoader(mContext, colorsFile);
        mBackupRepoPackagesObserver = (packages) -> filter(mCurrentFilterQuery);
        mColorRepo.getPackages().observeForever(mBackupRepoPackagesObserver);
        Log.i("ColorsViewModel", String.format("colors file %s", file.getAbsolutePath()));

    }

    public void deleteColor(int position) {
        mUpdatedColors = mColorRepo.getPackages().getValue();
        ColorMeta colorMeta = new ColorMeta.Builder(null).setValue(null).setIcon(null).build();
        mUpdatedColors.set(position, colorMeta);
        mUpdatedColors.remove(position);
        isChanget = true;
        saveColor();
    }

    public void addNewColor(String name, String color) {
        mUpdatedColors = mColorRepo.getPackages().getValue();
        ColorMeta colorMeta = new ColorMeta.Builder(name).setValue(color).setIcon(color).build();
        mUpdatedColors.add(colorMeta);
        isChanget = true;
        saveColor();
    }

    public void setNewColor(int position, String name, String color) {
        mUpdatedColors = mColorRepo.getPackages().getValue();
        ColorMeta colorMeta = new ColorMeta.Builder(name).setValue(color).setIcon(color).build();
        mUpdatedColors.set(position, colorMeta);
        isChanget = true;
        saveColor();
    }

    public void filter(String query) {
        filter(new FilterQuery(query));
    }

    private void filter(FilterQuery filterQuery) {
        mCurrentFilterQuery = filterQuery;
        //TODO probably do something about this cuz concurrency. Tho it should still be fine
        mRawColors = mColorRepo.getPackages().getValue();
        mFilter.filter(filterQuery.serializeToString());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mColorRepo.getPackages().removeObserver(mBackupRepoPackagesObserver);
    }

    private static class FilterQuery {
        String query;

        FilterQuery(String query) {
            this.query = query;
        }

        static FilterQuery fromString(String serializedFilterQuery) {
            String query = serializedFilterQuery;//.substring(2);
            return new FilterQuery(query);
        }

        String serializeToString() {
            return query;
        }
    }

    public void saveColor() {
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
            List<ColorMeta> colors = new ArrayList<>(mUpdatedColors);
            for (ColorMeta colorMeta : colors) {
                String stringId = colorMeta.label;
                String stringText = colorMeta.value;

                //String temp = translatorfunction(mo, lenguage);
                if (stringId != null && stringText != null) {
                    Element stringelement = doc.createElement("color"); // Create string in document
                    Attr attrType = doc.createAttribute("name"); // Create atribute name
                    attrType.setValue(stringId); // Add to "name" the word code
                    stringelement.setAttributeNode(attrType); // Add atribute to string elemt
                    stringelement.appendChild(doc.createTextNode(stringText)); // Add translated word to string
                    rootElement.appendChild(stringelement); // Add string element to document
                }
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
            FileOutputStream fileOutputStream = new FileOutputStream(colorsFile); // Write file
            transformer.transform(source, new StreamResult(fileOutputStream));

            // Output to console for testing
            // StreamResult consoleResult = new StreamResult(System.out);
            //transformer.transform(source, consoleResult);

        } catch (Exception e) {
            //Toast.makeText(App.getContext(), getResources().getString(R.string.toast_error_translate_language), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private class ColorFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ColorMeta> colors = new ArrayList<>(isChanget ? mUpdatedColors : mRawColors);
            FilterQuery filterQuery = FilterQuery.fromString(constraint.toString());
            String query = filterQuery.query.toLowerCase();

            Iterator<ColorMeta> iterator = colors.iterator();
            while (iterator.hasNext()) {
                ColorMeta packageMeta = iterator.next();
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
            results.values = new ArrayList<>(colors);
            results.count = colors.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mPackagesLiveData.setValue((List<ColorMeta>) results.values);
        }
    }
}

