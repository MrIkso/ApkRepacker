package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.SysUtils;
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

public class StringsFragment extends Fragment implements AddLanguageDialogFragment.ItemClickListener, StringsAdapter.OnItemClickListener {

    public static final String TAG = "StringsFragment";
    // Constants
    protected static final String encoding = "utf-8";
    private AppCompatSpinner langSpinner;

    private Map<String, String> strings = new LinkedHashMap<>();
    private StringsAdapter stringsAdapter;
    private ListView listView;
    private ArrayList<StringFile> files;
    private String projectPatch;
    private FloatingActionButton fabAddLanguage, fabfilterString, fabSave, fabAutoTranslate;

    private FloatingActionMenu fabMenu;
    private EditText searchText;
    private boolean isUp, isnoFullApk;
    private Map<String, String> dataTraslated = new HashMap<>();
    private List<String> strArr = new ArrayList<>();
    private List<String> langFiles = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
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
        if (new File(projectPatch, "resources.arsc").exists() | !new File(projectPatch, "res").exists()) {
            isnoFullApk = true;
        } else {
            DirectoryScanner scanner = new DirectoryScanner();
            files = scanner.findStringFiles(projectPatch);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_strings, container, false);
        mContext = view.getContext();
        FrameLayout searhView = view.findViewById(R.id.search_view);
        isUp = false;
        listView = view.findViewById(R.id.string_list);
        //new FastScrollerBuilder(listView).build();
        searchText = view.findViewById(R.id.search_text);
        // listView.setTextFilterEnabled(true);
        langSpinner = view.findViewById(R.id.language_spinner);
        fabMenu = view.findViewById(R.id.fab);
        fabfilterString = view.findViewById(R.id.fab_filter);
        fabSave = view.findViewById(R.id.fab_save_language);
        fabAddLanguage = view.findViewById(R.id.fab_add_language);
        fabAutoTranslate = view.findViewById(R.id.fab_auto_translate_language);
        if (!isnoFullApk) {
            fabfilterString.setOnClickListener(v -> {
                if (isUp) {
                    slideDown(searhView);
                    fabMenu.close(true);
                    stringsAdapter.getFilter().filter("");
                    StringUtils.hideKeyboard(this);
                } else {
                    slideUp(searhView);
                    fabMenu.close(true);
                }
                isUp = !isUp;
            });
            fabAddLanguage.setOnClickListener(v -> {
                fabMenu.close(true);
                AddLanguageDialogFragment fragment = AddLanguageDialogFragment.newInstance();
                fragment.show(getChildFragmentManager(), AddLanguageDialogFragment.TAG);
            });
            fabAutoTranslate.setOnClickListener(v -> {
                fabMenu.close(true);
                String selectedLang = langSpinner.getSelectedItem().toString();
                List<TranslateItem> stringValues = new ArrayList();
                if (selectedLang.equals("default")) {
                    mTargetLang = "-auto";
                } else {
                    mTargetLang = "-" + langSpinner.getSelectedItem().toString();
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
            fabSave.setOnClickListener(v -> {
                fabMenu.close(true);
                if (dataTraslated != null) {
                    try {
                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        String selectedLang = langSpinner.getSelectedItem().toString();
                        if (selectedLang.equals("default")) {
                            translateArray(dataTraslated, "");
                        } else {
                            translateArray(dataTraslated, "-" + langSpinner.getSelectedItem().toString());
                        }

                        //  }
                    } catch (ParserConfigurationException e) {
                        UIUtils.toast(App.getContext(), getResources().getString(R.string.toast_error_translate_language));
                        e.printStackTrace();
                    }
                }
            });
        }
        fabMenu.setClosedOnTouchOutside(true);

        if (!isnoFullApk) {
            initList();
        }
        return view;
    }

    private void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animation = new TranslateAnimation(0, 0, view.getHeight(), 0);
        animation.setDuration(400);//speed animation 500 ms default
        animation.setFillAfter(true);
        view.startAnimation(animation);
        fabMenu.setPadding(SysUtils.dpToPixels(mContext, 10), SysUtils.dpToPixels(mContext, 10),
                SysUtils.dpToPixels(mContext, 10), SysUtils.dpToPixels(mContext, 40));
    }

    private void slideDown(View view) {
        view.setVisibility(View.GONE);
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, view.getHeight());
        animation.setDuration(400);//speed animation 500 ms default
        animation.setFillAfter(true);
        view.startAnimation(animation);
        fabMenu.setPadding(SysUtils.dpToPixels(mContext, 10), SysUtils.dpToPixels(mContext, 10),
                SysUtils.dpToPixels(mContext, 10), SysUtils.dpToPixels(mContext, 10));
    }

    private void initList() {

        for (StringFile a : files) {
            try {
                strArr.add(a.lang());
                langFiles.add(a.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stringsAdapter = new StringsAdapter(strings);
        stringsAdapter.setInteractionListener(this);
        parseStings(new File(langFiles.get(0)));
        spinnerAdapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_spinner_item, strArr);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        langSpinner.setAdapter(spinnerAdapter);
        /*     langSpinner.setOnItemClickListener((parent, view, position, id) -> {
            if(!dataTraslated.isEmpty()) {
                UIUtils.showConfirmDialog(mContext, getResources().getString(R.string.confirm_save), new UIUtils.OnClickCallback() {
                    @Override
                    public void onOkClick() {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                translateArray(dataTraslated, langSpinner.getSelectedItem().toString());
                            }
                        } catch (IOException | ParserConfigurationException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });*/
        langSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                strings.clear();
                parseStings(new File(langFiles.get(position)));
                reloadAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                stringsAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        listView.setAdapter(stringsAdapter);
    }

    private void reloadAdapter() {
        //  stringsAdapter = new StringsAdapter(strings);
        stringsAdapter.setInteractionListener(this);
        stringsAdapter.notifyDataSetChanged();
        listView.setAdapter(stringsAdapter);
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

    @Override
    public void onAddLangClick(String item) {
        try {
            File newLang = new File(projectPatch + "/res/values" + item, "strings.xml");
            if (!newLang.exists()) {
                // File newLang = new File(projectPatch+ "/res/values"+item, "strings.xml");
                FileUtils.copyFile(new File(projectPatch + "/res/values", "strings.xml"),
                        newLang);
                strings.clear();
                strArr.add(item.substring(1));
                langFiles.add(newLang.getCanonicalPath());
                parseStings(newLang);
                stringsAdapter.notifyDataSetChanged();
                langSpinner.setSelection(strArr.indexOf(item.substring(1)));
                reloadAdapter();
            } else {
                UIUtils.toast(App.getContext(), R.string.toast_error_new_language_is_exits);
            }
        } catch (IOException e) {
            UIUtils.toast(App.getContext(), getResources().getString(R.string.toast_error_create_new_language));
            e.printStackTrace();
        }
    }

    private void trastaleValue(String key, String value) {

        if (dataTraslated.containsKey(key)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dataTraslated.replace(key, value);
            } else {
                dataTraslated.remove(key);
                dataTraslated.put(key, value);
            }
        } else {
            dataTraslated.put(key, value);
        }
        stringsAdapter.setUpdateValue(key, value);

    }

    //   @RequiresApi(api = Build.VERSION_CODES.N)
    private void translateArray(Map<String, String> translation, String language) throws ParserConfigurationException  // Create the xml
    {
        Map<String, String> result = new HashMap<>(translation);
        for (Map.Entry<String, String> e : strings.entrySet()) {
            if (!translation.containsKey(e.getKey())) {
                //  result.merge(e.getKey(), e.getValue(), String::concat);
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
        trastaleValue(key, value);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
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
                DLog.d(stringId +" "+stringText );
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