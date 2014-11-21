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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import lp.edu.ua.sopushynskyi.dicom.NetworkService;

public class PatientsDialog extends AlertDialog.Builder {
    private String url;

    private ListView listView;
    private EditText patientEdit;


    private List<Patient> patientList = new LinkedList<Patient>();

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
                final ArrayAdapter<Patient> adapter = (PatientAdapter) adapterView.getAdapter();

                Patient patient = adapter.getItem(i);
                Toast.makeText(getContext(), "Клік по пацієнту " + patient.getName(), Toast.LENGTH_SHORT).show();
            }
        });
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
                    Toast.makeText(getContext(), "Немає підключення до мережі", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return dialog;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager)getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(patientEdit.getWindowToken(), 0);
    }

    private class GetPatientListTask extends AsyncTask<String, Void, String> {
        private final String ATTR_ID = "id";

        private final String ATTR_NAME = "name";

        @Override
        protected String doInBackground(String... urls) {
            NetworkService network = new NetworkService();
            return network.makeServiceCall(urls[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            String id;
            String name;

            try {
                patientList.clear();
                JSONArray array = new JSONArray(result);
                JSONObject element;

                for (int i = 0; i < array.length(); i++) {
                    element = (JSONObject) array.get(i);
                    id = element.optString(ATTR_ID);
                    name = element.optString(ATTR_NAME);

                    patientList.add(new Patient(id, name));
                }

                final PatientAdapter adapter = new PatientAdapter(getContext(), patientList);
                listView.setAdapter(adapter);
                hideKeyBoard();

                Toast.makeText(getContext(), "Пацієнти знайдені", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class PatientAdapter extends ArrayAdapter<Patient> {
        List<Patient> patients;
        Context context;

        public PatientAdapter(Context context, List<Patient> patients) {
            super(context, R.layout.patient_element, patients);

            this.patients = patients;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View patientView = inflater.inflate(R.layout.patient_element, parent, false);
            TextView patientNameView = (TextView) patientView.findViewById(R.id.patientName);
            TextView patientIdView = (TextView) patientView.findViewById(R.id.patientId);

            final Patient patient = patients.get(position);

            patientNameView.setText(patient.getName());
            patientIdView.setText("ID: " + patient.getId());

            return patientView;
        }
    }
}
