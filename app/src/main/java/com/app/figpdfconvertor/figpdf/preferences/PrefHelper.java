package com.app.figpdfconvertor.figpdf.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.figpdfconvertor.figpdf.utils.MyApp;

public class PrefHelper {

    public static PrefHelper instance;
    private final SharedPreferences.Editor editor;
    private final SharedPreferences settings;

    private PrefHelper() {
        SharedPreferences sharedPreferences = MyApp.getInstance().getSharedPreferences("Fig PDF Convertor", Context.MODE_PRIVATE);
        this.settings = sharedPreferences;
        this.editor = sharedPreferences.edit();
    }

    public static PrefHelper getInstance() {
        if (instance == null) {
            instance = new PrefHelper();
        }
        return instance;
    }

    public String getString(String key, String defValue) {
        return this.settings.getString(key, defValue);
    }

    public PrefHelper setString(String key, String value) {
        this.editor.putString(key, value);
        this.editor.commit();
        return this;
    }

    public int getInt(String key, int defValue) {
        return this.settings.getInt(key, defValue);
    }

    public PrefHelper setInt(String key, int value) {
        this.editor.putInt(key, value);
        this.editor.commit();
        return this;
    }

    public boolean getBoolean(String key, boolean defValue) {
        return this.settings.getBoolean(key, defValue);
    }

    public PrefHelper setBoolean(String key, boolean value) {
        this.editor.putBoolean(key, value);
        this.editor.commit();
        return this;
    }

    public void clear() {
        this.editor.clear();
        this.editor.commit();
    }
}