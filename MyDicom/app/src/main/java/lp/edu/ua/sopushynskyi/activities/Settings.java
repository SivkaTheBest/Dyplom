package lp.edu.ua.sopushynskyi.activities;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mykola.mydicom.R;

public class Settings {
    private static SharedPreferences sharedPref;

    public static String loadServerURL(Context context) {
        String defaultServerURL = context.getString(R.string.defaultServerURL);
        sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        String serverURL = sharedPref.getString(context.getString(R.string.preference_server_url), defaultServerURL);
        return serverURL;
    }

    public static void saveServerURL(Context context, String url) {
        sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.preference_server_url), url);
        editor.commit();
    }
}
