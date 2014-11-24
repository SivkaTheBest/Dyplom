package lp.edu.ua.sopushynskyi.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.mykola.mydicom.R;

import lp.edu.ua.sopushynskyi.activities.Settings;

public class SettingsDialog extends AlertDialog.Builder {
    private EditText serverURLEdit;
    private Context context;

    public SettingsDialog(final Context context) {
        super(context);
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.app_settings, null);
        serverURLEdit = (EditText) view.findViewById(R.id.serverURL);

        setView(view)
                .setTitle(context.getString(R.string.settingsTitle))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Settings.saveServerURL(context, serverURLEdit.getText().toString());
                    }
                });

        serverURLEdit.setText(Settings.loadServerURL(context));
    }
}
