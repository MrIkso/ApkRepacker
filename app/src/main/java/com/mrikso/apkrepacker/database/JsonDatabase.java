/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mrikso.apkrepacker.database;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrikso.apkrepacker.database.entity.FindKeywordsAndFilesItem;
import com.mrikso.apkrepacker.database.entity.FindKeywordsItem;
import com.mrikso.apkrepacker.database.entity.Project;
import com.mrikso.apkrepacker.database.entity.RecentFileItem;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.ProjectUtils;
import com.mrikso.apkrepacker.utils.common.DLog;


import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class JsonDatabase implements ITabDatabase {

    private final String RECENT_FILES_DATABASE_NAME = "recent_files.json";
    private final String KEYWORDS_DATABASE_NAME = "keywords.json";
    private final String KEYWORDS_FILES_DATABASE_NAME = "keywords_files.json";
    private final String KEYWORDS_PROJECT_DATABASE_NAME = "project.json";
    private RecentFileJsonHelper mHelper;
    private Context mContext;
    private File mProjectDatabasePath;

    public JsonDatabase(Context context) {
        mContext = context;
        mHelper = new RecentFileJsonHelper();
        mProjectDatabasePath = new File(ProjectUtils.getProjectPath() + File.separator + ".database" + File.separator);
    }

    public static ITabDatabase getInstance(Context context) {
        return new JsonDatabase(context.getApplicationContext());
    }

    @Override
    public void addRecentFile(String path, String encoding) {
        if (TextUtils.isEmpty(path))
            return;
        try {
            JSONObject database = getRecentFileDatabase();
            JSONObject jsonItem;
            RecentFileItem recentFile;
            if (database.has(path)) {
                jsonItem = database.getJSONObject(path);
                recentFile = mHelper.read(jsonItem);
                recentFile.setPath(path);
                recentFile.setLastOpen(true);
            } else {
                jsonItem = new JSONObject();
                recentFile = new RecentFileItem();
                recentFile.setPath(path);
                recentFile.setEncoding(encoding);
                recentFile.setLastOpen(true);
                recentFile.setTime(System.currentTimeMillis());
                database.put(path, jsonItem);
            }
            mHelper.write(jsonItem, recentFile);
            saveRecentFileDatabase(database);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateRecentFile(String path, boolean lastOpen) {
        try {
            JSONObject database = getRecentFileDatabase();
            JSONObject jsonItem;
            RecentFileItem recentFile;
            if (database.has(path)) {
                jsonItem = database.getJSONObject(path);
                recentFile = mHelper.read(jsonItem);
                recentFile.setPath(path);
                recentFile.setLastOpen(lastOpen);

                mHelper.write(jsonItem, recentFile);
                saveRecentFileDatabase(database);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateRecentFile(String path, String encoding, int offset) {
        try {
            JSONObject database = getRecentFileDatabase();
            JSONObject jsonItem;
            RecentFileItem recentFile;
            if (database.has(path)) {
                jsonItem = database.getJSONObject(path);
                recentFile = mHelper.read(jsonItem);
                recentFile.setPath(path);
                recentFile.setOffset(offset);
                mHelper.write(jsonItem, recentFile);
                saveRecentFileDatabase(database);
            } else {
                addRecentFile(path, encoding);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<RecentFileItem> getRecentFiles() {
        return getRecentFiles(false);
    }

    @Override
    public ArrayList<RecentFileItem> getRecentFiles(boolean lastOpenFiles) {
        ArrayList<RecentFileItem> list = new ArrayList<>();
        JSONObject db = getRecentFileDatabase();
        Iterator<String> keys = db.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                JSONObject jsonObject = db.getJSONObject(key);
                RecentFileItem file = mHelper.read(jsonObject);
                if (file.isLastOpen() == lastOpenFiles) {
                    list.add(file);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public void clearRecentFiles() {
        saveRecentFileDatabase(new JSONObject());
    }

    @Override
    public void addFindKeyword(String keyword, boolean isReplace) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            File database = new File(mProjectDatabasePath.getAbsolutePath() + File.separator + KEYWORDS_DATABASE_NAME);
            FindKeywordsItem recent = gson.fromJson(getKeywordsDatabase(), FindKeywordsItem.class);
            if (isReplace) {
                if (!recent.getReplaceKeyword().isEmpty()) {
                    if (!recent.getReplaceKeyword().contains(keyword))
                        recent.setReplaceKeyword(keyword);
                } else {
                    recent.setReplaceKeyword(keyword);
                }
            } else {
                if (!recent.getKeyword().isEmpty()) {
                    if (!recent.getKeyword().contains(keyword))
                        recent.setKeyword(keyword);
                } else {
                    recent.setKeyword(keyword);
                }
            }

            FileWriter writer = new FileWriter(database);
            gson.toJson(recent, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addFindKeyword(List<String> keyword, boolean isReplace) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            File database = new File(mProjectDatabasePath.getAbsolutePath() + File.separator + KEYWORDS_DATABASE_NAME);
            FindKeywordsItem recent = gson.fromJson(getKeywordsDatabase(), FindKeywordsItem.class);
            if (isReplace) {
                if(Collections.disjoint(recent.getReplaceKeyword(), keyword))
                recent.getReplaceKeyword().clear();
                else
                recent.setReplaceKeyword(keyword);
            } else {
                if(Collections.disjoint(recent.getKeyword(), keyword))
                recent.getKeyword().clear();
                else
                recent.setKeyword(keyword);
            }

            FileWriter writer = new FileWriter(database);
            gson.toJson(recent, writer);
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ArrayList<String> getFindKeywords(boolean isReplace) {
        ArrayList<String> list = new ArrayList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FindKeywordsItem recent = gson.fromJson(getKeywordsDatabase(), FindKeywordsItem.class);
        if (isReplace) {
            list.addAll(recent.getReplaceKeyword());
        } else {
            list.addAll(recent.getKeyword());
        }
        if (list.isEmpty()) {
            list.add("");
        }
        return list;
    }

    @Override
    public void addFindKeywordAndFiles(String keyword, boolean isFiles) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            File database = new File(mProjectDatabasePath.getAbsolutePath() + File.separator + KEYWORDS_FILES_DATABASE_NAME);
            FindKeywordsAndFilesItem recent = gson.fromJson(getKeywordsAndFilesDatabase(), FindKeywordsAndFilesItem.class);
            if (isFiles) {
                if (!recent.getFilesKeyword().isEmpty()) {
                    if (!recent.getFilesKeyword().contains(keyword))
                        recent.setFilesKeyword(keyword);
                } else {
                    recent.setFilesKeyword(keyword);
                }
            } else {
                if (!recent.getKeyword().isEmpty()) {
                    if (!recent.getKeyword().contains(keyword))
                        recent.setKeyword(keyword);
                } else {
                    recent.setKeyword(keyword);
                }
            }

            FileWriter writer = new FileWriter(database);
            gson.toJson(recent, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addFindKeywordAndFiles(List<String> keyword, boolean isFile) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            File database = new File(mProjectDatabasePath.getAbsolutePath() + File.separator + KEYWORDS_FILES_DATABASE_NAME);
            FindKeywordsAndFilesItem recent = gson.fromJson(getKeywordsAndFilesDatabase(), FindKeywordsAndFilesItem.class);
            if (isFile) {
                if(Collections.disjoint(recent.getFilesKeyword(), keyword))
                    recent.getFilesKeyword().clear();
                else
                    recent.setFilesKeyword(keyword);
            } else {
                if(Collections.disjoint(recent.getKeyword(), keyword))
                    recent.getKeyword().clear();
                else
                    recent.setKeyword(keyword);
            }

            FileWriter writer = new FileWriter(database);
            gson.toJson(recent, writer);
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ArrayList<String> getFindKeywordsAdnFile(boolean isFiles) {
        ArrayList<String> list = new ArrayList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FindKeywordsAndFilesItem recent = gson.fromJson(getKeywordsAndFilesDatabase(), FindKeywordsAndFilesItem.class);
        if (isFiles) {
            list.addAll(recent.getFilesKeyword());
        } else {
            list.addAll(recent.getKeyword());
        }
        if (list.isEmpty()) {
            list.add("");
        }
        return list;
    }

    @Override
    public Project getProject(String path) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Project project = gson.fromJson(getProjectDatabase(path), Project.class);
            return project;
    }

    @Override
    public void addProjectNotes(String notes, String path) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            File database = new File(path +"/.database" + File.separator + KEYWORDS_PROJECT_DATABASE_NAME);

            Project recent = gson.fromJson(getProjectDatabase(path), Project.class);
            recent.setProjectNotes(notes);
       //     recent.setProjectName("");

            FileWriter writer = new FileWriter(database);
            gson.toJson(recent, writer);
            writer.flush();
            writer.close();
        } catch (Exception ex){
            DLog.e(ex.fillInStackTrace());
        }
    }

    @Override
    public String getProjectNotes(String path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Project project = gson.fromJson(getProjectDatabase(path), Project.class);
        return project.getProjectNotes();
    }

    private String getProjectDatabase(String path) {
        try {
            File database = new File(path + "/.database" + File.separator + KEYWORDS_PROJECT_DATABASE_NAME);
            if (!database.exists()) {
                database.getParentFile().mkdirs();
                database.createNewFile();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                FileWriter writer = new FileWriter(database);
                Project item = new Project();
                item.setProjectName("");
                item.setProjectNotes("");
                gson.toJson(item, writer);
                writer.flush();
                writer.close();
            }
                FileInputStream input = new FileInputStream(database);
                return IOUtils.toString(input, StandardCharsets.UTF_8);

        } catch (Exception ex) {
            DLog.e(ex.fillInStackTrace());
        }
        return "";
    }

    private String getKeywordsDatabase() {
        try {
            File database = new File(mProjectDatabasePath.getAbsolutePath() + File.separator + KEYWORDS_DATABASE_NAME);
            if (!database.exists()) {
                database.getParentFile().mkdirs();
                database.createNewFile();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                FileWriter writer = new FileWriter(database);
                FindKeywordsItem item = new FindKeywordsItem();
                item.setKeyword("");
                item.setReplaceKeyword("");
                gson.toJson(item, writer);
                writer.flush();
                writer.close();
            }
            FileInputStream input = new FileInputStream(database);
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            DLog.e(ex.fillInStackTrace());
        }
        return "";
    }

    private String getKeywordsAndFilesDatabase() {
        try {
            File database = new File(mProjectDatabasePath.getAbsolutePath() + File.separator + KEYWORDS_FILES_DATABASE_NAME);
            if (!database.exists()) {
                database.getParentFile().mkdirs();
                database.createNewFile();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                FileWriter writer = new FileWriter(database);
                FindKeywordsAndFilesItem item = new FindKeywordsAndFilesItem();
                item.setKeyword("");
                item.setFilesKeyword("");
                gson.toJson(item, writer);
                writer.flush();
                writer.close();
            }
            FileInputStream input = new FileInputStream(database);
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            DLog.e(ex.fillInStackTrace());
        }
        return "";
    }

    private void writeJsonToFile(JSONObject jsonObject, String fileName) {
        try {
            File file = new File(mProjectDatabasePath.getAbsolutePath() + File.separator + fileName);
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream output = new FileOutputStream(file);
            IOUtils.write(jsonObject.toString(), output, StandardCharsets.UTF_8);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveRecentFileDatabase(JSONObject database) {
        writeJsonToFile(database, RECENT_FILES_DATABASE_NAME);
    }

    private JSONObject readFromFile(String fileName) {
        try {
            File database = new File(mProjectDatabasePath.getAbsolutePath() + File.separator + fileName);
            if (!database.exists()) {
                database.createNewFile();
            }
            FileInputStream input = new FileInputStream(database);
            String content = IOUtils.toString(input, StandardCharsets.UTF_8);
            input.close();
            return new JSONObject(content);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    private JSONObject getRecentFileDatabase() {
        return readFromFile(RECENT_FILES_DATABASE_NAME);
    }

    private static class RecentFileJsonHelper {

        public void write(JSONObject json, RecentFileItem item) throws JSONException {
            json.put("time", item.time);
            json.put("path", item.path);
            json.put("encoding", item.encoding);
            json.put("offset", item.offset);
            json.put("isLastOpen", item.isLastOpen);
        }

        public RecentFileItem read(JSONObject json) throws JSONException {
            RecentFileItem item = new RecentFileItem();
            if (json.has("time")) {
                item.time = json.getInt("time");
            }
            if (json.has("path")) {
                item.path = json.getString("path");
            }
            if (json.has("encoding")) {
                item.encoding = json.getString("encoding");
            }
            if (json.has("offset")) {
                item.offset = json.getInt("offset");
            }
            if (json.has("isLastOpen")) {
                item.isLastOpen = json.getBoolean("isLastOpen");
            }
            return item;
        }
    }

}
