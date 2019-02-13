package tk.cs8898.elfofflinett.testabstraction;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

class TestSharedPreferences implements SharedPreferences {
    private static TestSharedPreferences instance;

    private final Map<String,Object> prefs = new HashMap<>();
    private final Editor editor = new TestEditor(this);
    static TestSharedPreferences getInstance(){
        if(instance == null){
            instance = new TestSharedPreferences();
        }
        return instance;
    }

    @Override
    public Map<String, ?> getAll() {
        return prefs;
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return (String) prefs.getOrDefault(key,defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return (Set<String>) prefs.getOrDefault(key,defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        return (int) prefs.getOrDefault(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return (long) prefs.getOrDefault(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return (float) prefs.getOrDefault(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return (boolean) prefs.getOrDefault(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return prefs.containsKey(key);
    }

    @Override
    public Editor edit() {
        return editor;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

    }

    private class TestEditor implements Editor {
        private TestSharedPreferences prefs;
        TestEditor(TestSharedPreferences prefs){
            this.prefs = prefs;
        }

        @Override
        public Editor putString(String key, @Nullable String value) {
            prefs.prefs.put(key,value);
            return this;
        }

        @Override
        public Editor putStringSet(String key, @Nullable Set<String> values) {
            prefs.prefs.put(key,values);
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            prefs.prefs.put(key,value);
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            prefs.prefs.put(key,value);
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            prefs.prefs.put(key,value);
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            prefs.prefs.put(key,value);
            return this;
        }

        @Override
        public Editor remove(String key) {
            prefs.prefs.remove(key);
            return this;
        }

        @Override
        public Editor clear() {
            prefs.prefs.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return true;
        }

        @Override
        public void apply() {
        }
    }
}
