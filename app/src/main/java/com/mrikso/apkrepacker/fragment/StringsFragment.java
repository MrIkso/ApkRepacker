package com.mrikso.apkrepacker.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.AutoTranslatorActivity;
import com.mrikso.apkrepacker.autotranslator.common.TranslateStringsHelper;
import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;
import com.mrikso.apkrepacker.fragment.dialogs.AddLanguageDialogFragment;
import com.mrikso.apkrepacker.ui.stringlist.DirectoryScanner;
import com.mrikso.apkrepacker.ui.stringlist.StringFile;
import com.mrikso.apkrepacker.ui.stringlist.StringsAdapter;
import com.mrikso.apkrepacker.utils.StringUtils;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class StringsFragment extends Fragment implements AddLanguageDialogFragment.ItemClickListener, StringsAdapter.OnItemClickListener {

    public static final String TAG = "StringsFragment";
    // Constants
    protected static final String encoding = "utf-8";
    private String selectedLanguage = "default";

    private Map<String, String> strings = new LinkedHashMap<>();
    private StringsAdapter stringsAdapter;
    private RecyclerView recyclerView;
    private ArrayList<StringFile> files;
    private String projectPatch;
    private FloatingActionButton fabAddLanguage, fabSelectLanguage, fabAutoTranslate;
    ;
    private FloatingActionMenu fabMenu;
    private AppCompatEditText searchText;
    private boolean isnoFullApk;
    private Map<String, String> dataTranslated = new HashMap<>();
    private List<String> strArr = new ArrayList<>();
    private List<String> langFiles = new ArrayList<>();
    private String[] arrayOfList;
    private Context mContext;
    private String mTargetLang;

    public StringsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            projectPatch = bundle.getString("prjPatch");
        }
        findStringFiles();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_strings, container, false);
        mContext = view.getContext();
        recyclerView = view.findViewById(R.id.string_list);
        searchText = view.findViewById(R.id.et_search);
        fabMenu = view.findViewById(R.id.fab);
        fabSelectLanguage = view.findViewById(R.id.fab_select_language);
        fabAddLanguage = view.findViewById(R.id.fab_add_language);
        fabAutoTranslate = view.findViewById(R.id.fab_auto_translate_language);
        if (!isnoFullApk) {
            fabAddLanguage.setOnClickListener(v -> {
                fabMenu.close(true);
                AddLanguageDialogFragment fragment = AddLanguageDialogFragment.newInstance();
                fragment.show(getChildFragmentManager(), AddLanguageDialogFragment.TAG);
            });
            fabAutoTranslate.setOnClickListener(v -> {
                fabMenu.close(true);
                List<TranslateItem> stringValues = new ArrayList();
                if (selectedLanguage.equals("default")) {
                    mTargetLang = "-auto";
                } else {
                    mTargetLang = "-" + selectedLanguage;
                }
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                try {
                    //get default language strings
                    DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
                    Document doc = documentBuilder.parse(new File(langFiles.get(0)));
                    doc.getDocumentElement().normalize();
                    NodeList nodeList = doc.getElementsByTagName("string");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) node;
                            String key = element.getAttribute("name");
                            String value = element.getTextContent();
                            strings.put(key, value);
                            stringValues.add(new TranslateItem(key, value, null));
                        }
                    }
                } catch (IOException | SAXException | ParserConfigurationException e) {
                    UIUtils.toast(App.getContext(), getResources().getString(R.string.toast_error_pasring));
                    e.printStackTrace();
                }
                // Fragment fragment = this;
                TranslateStringsHelper.setDefaultStrings(stringValues);
                Intent intent = new Intent(mContext, AutoTranslatorActivity.class);
                intent.putExtra("targetLanguageCode", mTargetLang);
                startActivityForResult(intent, 10);
            });
            fabSelectLanguage.setOnClickListener(v -> {
                fabMenu.close(true);
                findStringFiles();
                arrayOfList = new String[strArr.size()];
                strArr.toArray(arrayOfList);
                showListDialog(getContext(), arrayOfList);
            });
        }
        fabMenu.setClosedOnTouchOutside(true);

        if (!isnoFullApk && !files.isEmpty()) {
            initList(view);
        }

        return view;
    }

    private void findStringFiles() {
        if (new File(projectPatch, "resources.arsc").exists() | !new File(projectPatch, "res").exists()) {
            isnoFullApk = true;
        } else {
            DirectoryScanner scanner = new DirectoryScanner();
            files = scanner.findStringFiles(projectPatch);
        }

        if (!isnoFullApk && !files.isEmpty()) {
            strArr.clear();
            langFiles.clear();
            for (StringFile a : files) {
                try {
                    strArr.add(a.lang());
                    langFiles.add(a.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void initList(View view) {
        parseStings(new File(langFiles.get(0)));
        stringsAdapter = new StringsAdapter(getContext());
        stringsAdapter.setItems(strings);
        stringsAdapter.setInteractionListener(this);
        recyclerView.setAdapter(stringsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        new FastScrollerBuilder(recyclerView).useMd2Style().build();

//        arrayOfList = new String[strArr.size()];
//        strArr.toArray(arrayOfList);

        fabSelectLanguage.setLabelText(getString(R.string.action_select_lang, selectedLanguage));

        view.findViewById(R.id.button_clear).setOnClickListener(v -> searchText.setText(""));
        searchText.clearFocus();
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                view.findViewById(R.id.button_clear).setVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
                stringsAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void showListDialog(Context context, String[] list) {
        UIUtils.showListDialog(context, 0, 0, list, 0, new UIUtils.OnListCallback() {

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onSelect(MaterialDialog dialog, int which) {
                strings.clear();
                parseStings(new File(langFiles.get(which)));
                reloadAdapter();
                selectedLanguage = arrayOfList[which];
                fabSelectLanguage.setLabelText(getString(R.string.action_select_lang, selectedLanguage));
            }
        }, null);
    }

    private void reloadAdapter() {
        //  stringsAdapter = new StringsAdapter(strings);
        stringsAdapter.setInteractionListener(this);
        stringsAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(stringsAdapter);
    }

    private void parseStings(File file) {
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
                    strings.put(key, value);
                }
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            UIUtils.toast(App.getContext(), getResources().getString(R.string.toast_error_pasring));
            e.printStackTrace();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden)
            StringUtils.hideKeyboard(this);
    }

    @SuppressLint({"StringFormatMatches", "StringFormatInvalid"})
    @Override
    public void onAddLangClick(String item) {
        try {
            File newLang = new File(projectPatch + "/res/values" + item, "strings.xml");
            if (!newLang.exists()) {
                // File newLang = new File(projectPatch+ "/res/values"+item, "strings.xml");
                FileUtils.copyFile(new File(projectPatch + "/res/values", "strings.xml"), newLang);
                strings.clear();
                strArr.add(item.substring(1));
                langFiles.add(newLang.getCanonicalPath());
                parseStings(newLang);
                stringsAdapter.notifyDataSetChanged();
                int position = strArr.indexOf(item.substring(1));
                selectedLanguage = strArr.get(position);
                fabSelectLanguage.setLabelText(getString(R.string.action_select_lang, strArr.get(position)));
                reloadAdapter();
            } else {
                UIUtils.toast(App.getContext(), R.string.toast_error_new_language_is_exits);
            }
        } catch (IOException e) {
            UIUtils.toast(App.getContext(), getResources().getString(R.string.toast_error_create_new_language));
            e.printStackTrace();
        }
    }

    private void translateValue(String key, String value) {
        if (dataTranslated.containsKey(key)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dataTranslated.replace(key, value);
            } else {
                dataTranslated.remove(key);
                dataTranslated.put(key, value);
            }
        } else {
            dataTranslated.put(key, value);
        }
        stringsAdapter.setUpdateValue(key, value);
    }

    // Create the xml
    private void translateArray(Map<String, String> translation, String language) throws ParserConfigurationException {
        Map<String, String> result = new HashMap<>(translation);
        for (Map.Entry<String, String> e : strings.entrySet()) {
            if (!translation.containsKey(e.getKey())) {
                result.put(e.getKey(), e.getValue());
            }
        }
        Map<String, String> treeMap = new TreeMap<>(result);

        int tempnumber = 0; // Temp number for (<string name="...">)
        File resultFile = new File(projectPatch + "/res/values" + language + "/");
        resultFile.mkdirs(); // Create path
        DocumentBuilderFactory dbFactory;
        DocumentBuilder dBuilder;
        Document doc;

        dbFactory = DocumentBuilderFactory.newInstance();
        dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.newDocument();

        try {

            // root element
            Element rootElement = doc.createElement("resources"); // Create resources in document
            doc.appendChild(rootElement); // Add resources in document

            // string element

            for (Map.Entry<String, String> string : treeMap.entrySet()) {
                String stringId = string.getKey();
                String stringText = string.getValue();

                Element stringelement = doc.createElement("string"); // Create string in document
                Attr attrType = doc.createAttribute("name"); // Create atribute name
                attrType.setValue(stringId); // Add to "name" the word code
                stringelement.setAttributeNode(attrType); // Add atribute to string elemt
                stringelement.appendChild(doc.createTextNode(stringText)); // Add translated word to string
                rootElement.appendChild(stringelement); // Add string element to document

                tempnumber++;
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            //    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            DOMSource source = new DOMSource(doc);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(resultFile.getCanonicalPath() + "/strings.xml")); // Write file
            transformer.transform(source, new StreamResult(fileOutputStream));

            // Output to console for testing
            // StreamResult consoleResult = new StreamResult(System.out);
            //transformer.transform(source, consoleResult);

        } catch (Exception e) {
            UIUtils.toast(App.getContext(), getResources().getString(R.string.toast_error_translate_language));
            e.printStackTrace();
        }
    }

    @Override
    public void onTranslateClicked(String key, String value) {
        translateValue(key, value);
        if (dataTranslated != null) {
            try {
                if (selectedLanguage.equals("default")) {
                    translateArray(dataTranslated, "");
                } else {
                    translateArray(dataTranslated, "-" + selectedLanguage);
                }
            } catch (ParserConfigurationException e) {
                UIUtils.toast(App.getContext(), getResources().getString(R.string.toast_error_translate_language));
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 /*&& !TranslateStringsHelper.getTranslatedStrings().isEmpty()*/) {
            saveAutotranslatedStrings(TranslateStringsHelper.getTranslatedStrings(), mTargetLang.replace("-auto", ""));
        }
    }

    private void saveAutotranslatedStrings(List<TranslateItem> items, String language) {
        int tempnumber = 0; // Temp number for (<string name="...">)
        File resultFile = new File(projectPatch + "/res/values" + language + "/");
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
            for (TranslateItem item : items) {
                String stringId = item.name;
                String stringText = item.translatedValue;
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
            parseStings(resultString);
            reloadAdapter();
        } catch (Exception e) {
            UIUtils.toast(App.getContext(), getResources().getString(R.string.toast_error_translate_language));
            e.printStackTrace();
        }
    }
}