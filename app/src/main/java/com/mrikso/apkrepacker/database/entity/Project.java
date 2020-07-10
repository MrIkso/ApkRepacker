package com.mrikso.apkrepacker.database.entity;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Project {

    @SerializedName("project_name")
    @Expose
    private String mProjectName;

    @SerializedName("project_notes")
    @Expose
    private String mProjectNotes;

    public void setProjectNotes(String notes){
        mProjectNotes = notes;
    }

    @NonNull
    public String getProjectNotes(){
        return mProjectNotes;
    }

    @NonNull
    public String getProjectName(){
        return mProjectName;
    }

    public void setProjectName(String projectName){
        mProjectName = projectName;
    }


}
