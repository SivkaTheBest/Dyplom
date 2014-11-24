package lp.edu.ua.sopushynskyi.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.mykola.mydicom.R;

public class SettingsDialog extends AlertDialog.Builder {
    private EditText serverURLEdit;
    private Context context;
    private SharedPreferences sharedPref;

    public SettingsDialog(final Context context) {
        super(context);
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.app_settings, null);
        sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        serverURLEdit = (EditText) view.findViewById(R.id.serverURL);

        setView(view)
                .setTitle(context.getString(R.string.settingsTitle))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(context.getString(R.string.preference_server_url), serverURLEdit.getText().toString());
                        editor.commit();
                    }
                });

        loadSharedPreferences();
    }

    private void loadSharedPreferences() {
        String defaultServerURL = context.getString(R.string.defaultServerURL);
        String serverURL = sharedPref.getString(context.getString(R.string.preference_server_url), defaultServerURL);
        serverURLEdit.setText(serverURL);
    }
}
