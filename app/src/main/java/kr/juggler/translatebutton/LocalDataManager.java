package kr.juggler.translatebutton;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalDataManager {
    public static final String PREFERENCES_NAME = "rebuild_preference";
    private static final String DEFAULT_VALUE_STRING = "";

    private static SharedPreferences getPreferences(Context context) {

        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

    }

    /**
     * String 값 저장
     * @param context = getApplicationContext()
     * @param key = 키
     * @param value = 값
     */
    public static void setString(Context context, String key, String value) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key) {

        SharedPreferences prefs = getPreferences(context);

        return prefs.getString(key, DEFAULT_VALUE_STRING);
    }

}
