package lp.edu.ua.sopushynskyi.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mykola.mydicom.R;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import lp.edu.ua.sopushynskyi.activities.Settings;
import lp.edu.ua.sopushynskyi.dialogs.beans.PatientElement;
import lp.edu.ua.sopushynskyi.dicom.NetworkService;

public class PatientsDialog extends AlertDialog.Builder {
    private String url;

    private ListView listView;
    private EditText patientEdit;
    private PatientDataDialog patientDataDialog;
    private DialogListener listener;

    private List<PatientElement> patientElementList = new LinkedList<PatientElement>();

    public PatientsDialog(final Context context) {
        super(context);
        setCancelable(true);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.patients, null);

        setView(view)
                .setTitle(context.getString(R.string.findPatientTitle))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.findPatient, null);

        patientEdit = (EditText) view.findViewById(R.id.patientName);
        listView = (ListView) view.findViewById(R.id.patientsList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final ArrayAdapter<PatientElement> adapter = (PatientAdapter) adapterView.getAdapter();

                PatientElement patientElement = adapter.getItem(i);
                patientDataDialog = new PatientDataDialog(context, patientElement, url);

                patientDataDialog.setListener(new DialogListener() {
                    @Override
                    public void OnSelectedResult(String result) {
                        listener.OnSelectedResult(result);

                    }
                });

                patientDataDialog.show();
            }
        });

        url = Settings.loadServerURL(context);

    }

    @Override
    public AlertDialog show() {
        AlertDialog dialog = super.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ConnectivityManager connMgr = (ConnectivityManager)
                        getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new GetPatientListTask().execute(url);
                } else {
                    Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return dialog;
    }

    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager)getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(patientEdit.getWindowToken(), 0);
    }

    public void setListener(DialogListener listener) {
        this.listener = listener;
    }

    private class GetPatientListTask extends AsyncTask<String, Void, String> {
        private final String ATTR_ID = "id";
        private final NetworkService network = new NetworkService();;
        private final String ATTR_NAME = "name";

        @Override
        protected String doInBackground(String... urls) {
            return network.makeServiceCall(urls[0] + "/patients");
        }
        @Override
        protected void onPostExecute(String result) {
            String id;
            String name;

            try {
                if(!StringUtils.isEmpty(result)) {
                    patientElementList.clear();
                    JSONArray array = new JSONArray(result);
                    JSONObject element;

                    for (int i = 0; i < array.length(); i++) {
                        element = (JSONObject) array.get(i);
                        id = element.optString(ATTR_ID);
                        name = element.optString(ATTR_NAME);

                        patientElementList.add(new PatientElement(id, name));
                    }

                    final PatientAdapter adapter = new PatientAdapter(getContext(), patientElementList);
                    listView.setAdapter(adapter);
                    hideKeyBoard();

                    Toast.makeText(getContext(), "Пацієнти знайдені", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Неможливо отримати список пацієнтів", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class PatientAdapter extends ArrayAdapter<PatientElement> {
        List<PatientElement> patientElements;
        Context context;

        public PatientAdapter(Context context, List<PatientElement> patientElements) {
            super(context, R.layout.patient_element, patientElements);

            this.patientElements = patientElements;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View patientView = inflater.inflate(R.layout.patient_element, parent, false);
            TextView patientNameView = (TextView) patientView.findViewById(R.id.patientName);
            TextView patientIdView = (TextView) patientView.findViewById(R.id.patientId);

            final PatientElement patientElement = patientElements.get(position);

            patientNameView.setText(patientElement.getName());
            patientIdView.setText("ID: " + patientElement.getId());

            return patientView;
        }
    }
}
