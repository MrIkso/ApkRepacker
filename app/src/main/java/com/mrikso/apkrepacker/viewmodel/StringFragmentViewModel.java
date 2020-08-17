package com.mrikso.apkrepacker.viewmodel;

import android.app.Application;
import android.content.Context;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.autotranslator.dictionary.DictionaryItem;
import com.mrikso.apkrepacker.autotranslator.dictionary.DictionaryReader;
import com.mrikso.apkrepacker.autotranslator.dictionary.DictionaryWriter;
import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;
import com.mrikso.apkrepacker.ui.stringlist.DirectoryScanner;
import com.mrikso.apkrepacker.ui.stringlist.StringFile;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.ProjectUtils;
import com.mrikso.apkrepacker.utils.common.DLog;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class StringFragmentViewModel extends AndroidViewModel {

    protected static final String encoding = "utf-8";

    private Observer<List<TranslateItem>> mStringsObserver;
    private FilterQuery mCurrentFilterQuery = new FilterQuery("");
    private Filter mFilter;

  //  private List<TranslateItem> mRawStrings = new ArrayList<>();
    private List<TranslateItem> mUpdatedStrings = new ArrayList<>();
    private MutableLiveData<List<TranslateItem>> mTranslateItemMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<List<TranslateItem>> mTranslateItemMutableLiveDataOrig = new MutableLiveData<>();
    private MutableLiveData<List<StringFile>> mStringsFilesMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<String> mCurrentLanguage = new MutableLiveData<>("default");
    private String mProjectPath;
    private StringFile mCurrentLangFile;
    private boolean isChanget = false;
    private Context mContext;

    public StringFragmentViewModel(@NonNull Application application) {
        super(application);
        mContext = application;
        mProjectPath = ProjectUtils.getProjectPath();
        mFilter = new StringsFilter();
    }

    public void startLoad(){
        findStringFiles();
        //parse default language file
        mStringsObserver = (packages) -> filter(mCurrentFilterQuery);

        mTranslateItemMutableLiveDataOrig.observeForever(mStringsObserver);
        new Thread(() -> {
            parseStings(new StringFile(mProjectPath + "/res/values/strings.xml", "default"));
        }).start();
    }
    /**
     * ищем все строки локализаций
     */
    public void findStringFiles() {
        if (new File(mProjectPath, "resources.arsc").exists() | !new File(mProjectPath, "res").exists()) {
            return;
        } else {
            new Thread(() -> {
                DirectoryScanner scanner = new DirectoryScanner();
                mStringsFilesMutableLiveData.postValue(scanner.findStringFiles(mProjectPath));
            }).start();
        }
    }

    /**
     * получаем вмест файла строк
     */
    public LiveData<List<TranslateItem>> getStingsData() {
        return mTranslateItemMutableLiveData;
    }

    /**
     * получаем список файлов строк
     */
    public LiveData<List<StringFile>> getStingsFilesData() {
        return mStringsFilesMutableLiveData;
    }

    /**
     * возвращает текуший выбранный код локали
     */
    public LiveData<String> getCurrentLanguage() {
        return mCurrentLanguage;
    }

    /**
     * возвращает текуший выбранный файл
     */
    public StringFile getCurrentLangFile() {
        return mCurrentLangFile;
    }

    /**
     * парсим файл со строками
     */
    public void parseStings(StringFile file) {
        mCurrentLangFile = file;
        List<TranslateItem> stringValues = new ArrayList();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("string");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String key = element.getAttribute("name");
                    String value = element.getTextContent();
                    stringValues.add(new TranslateItem(key, value, null));
                }
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            UIUtils.toast(mContext, mContext.getString(R.string.toast_error_pasring));
            e.printStackTrace();
        }
        mCurrentLanguage.postValue(file.lang());
        mTranslateItemMutableLiveDataOrig.postValue(stringValues);
    //    startLoad();
    }

    /**
     * cохранение файла
     */
    public void saveStrings(List<TranslateItem> translatedList) {
        int tempnumber = 0; // Temp number for (<string name="...">)
        String fileName =  getCurrentLangFile().lang();
        File resultFile;
        if(fileName.startsWith("default")){
            resultFile = new File(ProjectUtils.getProjectPath() + "/res/values" + "/");
        }
        else {
            resultFile = new File(ProjectUtils.getProjectPath() + "/res/values" + "-" + getCurrentLangFile().lang() + "/");
        }
        resultFile.mkdirs(); // Create path
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
            for (TranslateItem item : translatedList) {
                String stringId = item.name;
                String stringText = item.translatedValue;
                if (stringText == null) {
                    stringText = item.originValue;
                }
                DLog.d(stringId + " " + stringText);
                Element stringelement = doc.createElement("string"); // Create string in document
                Attr attrType = doc.createAttribute("name"); // Create atribute name
                attrType.setValue(stringId); // Add to "name" the word code
                stringelement.setAttributeNode(attrType); // Add atribute to string elemt
                stringelement.appendChild(doc.createTextNode(stringText)); // Add translated word to string
                rootElement.appendChild(stringelement); // Add string element to document

                tempnumber++;
            }
            File resultString = new File(resultFile.getCanonicalPath() + "/strings.xml");
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            //    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            DOMSource source = new DOMSource(doc);
            FileOutputStream fileOutputStream = new FileOutputStream(resultString); // Write file
            transformer.transform(source, new StreamResult(fileOutputStream));
            parseStings(new StringFile(resultString.getAbsolutePath(), getCurrentLangFile().lang()));
            findStringFiles();
        } catch (Exception e) {
            UIUtils.toast(mContext, mContext.getString(R.string.toast_error_translate_language));
            e.printStackTrace();
        }
    }

    /**
     * добавляем новый язык на основе дефолтной локали
     *
     * @param lang код языка
     */
    public void addNewLang(String lang) {
        try {
            File newLang = new File(mProjectPath + "/res/values" + lang, "strings.xml");
            if (!newLang.exists()) {
                FileUtils.copyFile(new File(mProjectPath + "/res/values", "strings.xml"), newLang);
                parseStings(new StringFile(newLang.getAbsolutePath(), lang.substring(1)));
                findStringFiles();
            } else {
                UIUtils.toast(mContext, R.string.toast_error_new_language_is_exits);
            }
        } catch (IOException e) {
            UIUtils.toast(mContext, mContext.getString(R.string.toast_error_create_new_language));
            e.printStackTrace();
        }
    }

    /**
     * обновляем строку
     * @param position позиция в списке
     * @param newValue новое значение
     */
    public void updateString(int position, String newValue){
        isChanget = true;
        mTranslateItemMutableLiveData.getValue().get(position).translatedValue = newValue;
    }

    /**
     * удаляем стороку из списка
     * @param position позиция в списке
     */
    public void deleteString(int position){
        isChanget = true;
        mUpdatedStrings = mTranslateItemMutableLiveData.getValue();
        mUpdatedStrings.remove(position);
        saveStrings(mUpdatedStrings);
    }

    /**
     * добавляем новый елемент в список строк
     * @param item новый елемент
     */
    public void addNewString(TranslateItem item){
        isChanget = true;
        mUpdatedStrings = mTranslateItemMutableLiveData.getValue();
        mUpdatedStrings.add(item);
        saveStrings(mUpdatedStrings);
    }

   /* *//**
     * автопереводим на новый язык на основе дефолтной локали
     *
     * @param lang код языка
     *//*
    public void autoTranslate(String lang) {
        List<TranslateItem> stringValues = new ArrayList();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            //get default language strings
            DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(new File(mProjectPath + "/res/values", "strings.xml"));
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("string");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String key = element.getAttribute("name");
                    String value = element.getTextContent();
                    stringValues.add(new TranslateItem(key, value, null));
                }
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            UIUtils.toast(mContext, mContext.getString(R.string.toast_error_pasring));
            e.printStackTrace();
        }
        TranslateStringsHelper.setDefaultStrings(stringValues);
        Intent intent = new Intent(mContext, AutoTranslatorActivity.class);
        intent.putExtra("targetLanguageCode", lang);
        getApplication().startActivity(intent);
    }*/

    /**
     * сохраниение перевода в словарь
     *
     * @param dictionaryName     имя файла словаря
     * @param translatedList переведенный список строк
     */
    public void saveTranslationToDictionary(String dictionaryName, List<TranslateItem> translatedList) {
        File dictionaryDir = new File(FileUtil.getInternalStorage() + "/ApkRepacker/dictionary");
        if (!dictionaryDir.exists()) {
            dictionaryDir.mkdirs();
        }
        File dictionary = new File(dictionaryDir.getAbsolutePath() + "/" + dictionaryName + ".mtd");
        new DictionaryWriter(dictionary).writeDictionary(translatedList);
    }

    public void filter(String query) {
        filter(new FilterQuery(query));
    }

    private void filter(FilterQuery filterQuery) {
        DLog.d("called filter");
        mCurrentFilterQuery = filterQuery;
        mFilter.filter(filterQuery.serializeToString());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mTranslateItemMutableLiveDataOrig.removeObserver(mStringsObserver);
    }

    public void parseDictionary(String path){
        DictionaryReader dictionaryReader = new DictionaryReader(new File(path));
        dictionaryReader.readDictionary();
        List<TranslateItem> items = new ArrayList<>(isChanget ? mUpdatedStrings : mTranslateItemMutableLiveDataOrig.getValue());

        for(Map.Entry<String, DictionaryItem> entry: dictionaryReader.getDictionaryMap().entrySet()){
            String original = entry.getKey();
            String translated = entry.getValue().getTranslated();
            for(int i = 0; i <= items.size(); i++)
            {
                if(items.get(i).originValue.contains(original)){
                    updateString(i, translated);
                }
            }
            DLog.d(entry.getKey() + " => " +entry.getValue().getTranslated());
        }
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

    private class StringsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<TranslateItem> items = new ArrayList<>(isChanget ? mUpdatedStrings : mTranslateItemMutableLiveDataOrig.getValue());
                FilterQuery filterQuery = FilterQuery.fromString(constraint.toString());
                String query = filterQuery.query.toLowerCase();

                Iterator<TranslateItem> iterator = items.iterator();
                while (iterator.hasNext()) {
                    TranslateItem translateItem = iterator.next();
                    //Apply query
                    if (query.length() > 0) {
                        //Check if name matches
                        String[] wordsInLabel = translateItem.name.toLowerCase().split(" ");
                        boolean labelMatches = false;
                        for (String word : wordsInLabel) {
                            if (word.contains(query)) {
                                labelMatches = true;
                                break;
                            }
                        }

                        //Check if original value matches
                        boolean packagesMatches = translateItem.originValue.toLowerCase().contains(query);

                        if (!labelMatches && !packagesMatches)
                            iterator.remove();
                    }
                }

                FilterResults results = new FilterResults();
                results.values = new ArrayList<>(items);
                results.count = items.size();
                return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mTranslateItemMutableLiveData.setValue((List<TranslateItem>) results.values);
        }
    }
}
