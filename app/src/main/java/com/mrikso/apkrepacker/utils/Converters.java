package com.mrikso.apkrepacker.utils;



import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Converters {
  //  @TypeConverter
    public static ArrayList<String> fromString(String value){
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

   // @TypeConverter
    public static String fromArrayList(ArrayList<String> list){
        return new Gson().toJson(list);
    }
}
