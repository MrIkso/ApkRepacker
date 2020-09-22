package com.mrikso.patchengine.resource;

import androidx.annotation.NonNull;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ResourceItem {

    private int id;
    private String name;
    private String type;

    public ResourceItem(String type, String name, int _id) {
        this.type = type;
        this.name = name;
        this.id = _id;
    }

    public static List<ResourceItem> parseFrom(File line) {
        /*int startPos = 0;
        int endPos;
        int startPos2 = 0;
        int endPos2;
        int startPos3 = 0;
        int endPos3;
        String type2 = null;
        String name2 = null;
        int id2 = -1;
        int startPos4 = line.indexOf("type=\"");
        if (!(startPos4 == -1 || (endPos = line.indexOf("\" ", startPos)) == -1)) {
            type2 = line.substring(startPos, endPos);
            int startPos5 = line.indexOf("name=\"");
            if (!(startPos5 == -1 || (endPos2 = line.indexOf("\" ", startPos2)) == -1)) {
                name2 = line.substring(startPos2, endPos2);
                int startPos6 = line.indexOf("id=\"");
                if (!(startPos6 == -1 || (endPos3 = line.indexOf("\" ", startPos3)) == -1)) {
                    id2 = string2Id(line.substring(startPos3, endPos3));
                }
            }
        }
        if (type2 == null || name2 == null || id2 == -1) {
            return null;
        }*/
        try {
            List<ResourceItem> result = new ArrayList<>();
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(line);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("public");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String type = element.getAttribute("type");
                    String name = element.getAttribute("name");
                    String id = element.getAttribute("id");
                    result.add(new ResourceItem(type, name, string2Id(id)));
                }
            }
            return result;
        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
       return null;
    }

    public static int string2Id(String str) {
        int value = 0;
        if (str.length() == 10) {
            for (int i = 2; i < 10; i++) {
                value = (value << 4) | getVal(str.charAt(i));
            }
        }
        return value;
    }

    public static String id2String(int id2) {
        return "0x" + Integer.toHexString(id2);
    }

    private static int getVal(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 'a') + 10;
        }
        if (c < 'A' || c > 'F') {
            return 0;
        }
        return (c - 'A') + 10;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @NonNull
    public String toString() {
        return String.format("<public type=\"%s\" name=\"%s\" id=\"0x%s\" />", this.type, this.name, Integer.toHexString(this.id));
    }
}
