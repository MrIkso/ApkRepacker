package com.mrikso.apkrepacker.ui.publicxml;

import com.mrikso.apkrepacker.utils.ProjectUtils;
import com.mrikso.patchengine.resource.ResourceItem;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class PublicXmlParser {

    private File mPublicContent;
    private List<ResourceItem> mResourceItemList = new ArrayList<>();

    public PublicXmlParser(File content){
        mPublicContent = content;
        parse();
    }

    private void parse() {
        mResourceItemList.addAll(ResourceItem.parseFrom(mPublicContent));
    }

    public String getNameById(String id){
        for (ResourceItem item: mResourceItemList) {
            if (ResourceItem.id2String(item.getId()).equals(id)){
                return item.getName();
            }
        }
        return null;
    }

    public String getIdByName(String name){
        for (ResourceItem item: mResourceItemList) {
            String id = item.getName();
            if (id.contains(name)){
                return ResourceItem.id2String(item.getId());
            }
        }
        return null;
    }

    public void save(List<ResourceItem> list){
        int tempnumber = 0; // Temp number for (<string name="...">)
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
            List<ResourceItem> resourceItems = new ArrayList<>(list);
            for (ResourceItem item : resourceItems) {
                String type = item.getType();
                String name = item.getName();
                String stringId = ResourceItem.id2String(item.getId());

                if (stringId != null && name != null) {
                    Element stringelement = doc.createElement("public"); // Create string in document
                    Attr attrType = doc.createAttribute("type"); // Create atribute type
                    Attr attrName = doc.createAttribute("name"); // Create atribute name
                    Attr attrId = doc.createAttribute("id");
                    attrType.setValue(type); // Add to "name" the word code
                    attrName.setValue(name); // Add to "name" the word code
                    attrId.setValue(stringId); // Add to "name" the word code
                    stringelement.setAttributeNode(attrType); // Add atribute to string elemt
                    stringelement.setAttributeNode(attrName); // Add atribute to string elemt
                    stringelement.setAttributeNode(attrId); // Add atribute to string elemt
                //    stringelement.appendChild(doc.createTextNode(stringText)); // Add translated word to string
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
            FileOutputStream fileOutputStream = new FileOutputStream(ProjectUtils.getProjectPath() + "/res/values/public.xml"); // Write file
            transformer.transform(source, new StreamResult(fileOutputStream));

            // Output to console for testing
            // StreamResult consoleResult = new StreamResult(System.out);
            //transformer.transform(source, consoleResult);

        } catch (Exception e) {
            //Toast.makeText(App.getContext(), getResources().getString(R.string.toast_error_translate_language), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
