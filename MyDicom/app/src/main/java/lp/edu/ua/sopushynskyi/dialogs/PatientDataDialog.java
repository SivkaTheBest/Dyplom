package lp.edu.ua.sopushynskyi.dialogs;


import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mykola.mydicom.R;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import lp.edu.ua.sopushynskyi.dialogs.utils.PatientDataElement;
import lp.edu.ua.sopushynskyi.dialogs.utils.PatientElement;
import lp.edu.ua.sopushynskyi.dialogs.utils.TypeOfElement;
import lp.edu.ua.sopushynskyi.dicom.NetworkService;

public class PatientDataDialog extends AlertDialog.Builder{

    private PatientElement patientElement;
    private List<PatientDataElement> patientDataList = new LinkedList<PatientDataElement>();
    private ListView listView;
    private String url;

    public PatientDataDialog(final Context context, PatientElement patientElement, String url) {
        super(context);
        setCancelable(true);

        this.url = url;
        this.patientElement = patientElement;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.patient_data, null);

        setView(view)
                .setTitle(patientElement.toString())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.refresh, null);

        listView = (ListView) view.findViewById(R.id.patientsData);
        getPatientData();
    }

    @Override
    public AlertDialog show() {
        AlertDialog dialog = super.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getPatientData();
            }
        });
        return dialog;
    }

    private void getPatientData() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetPatientDataListTask().execute(url);
        } else {
            Toast.makeText(getContext(), "Немає підключення до мережі", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetPatientDataListTask extends AsyncTask<String, Void, String> {
        private final String ATTR_TYPE = "type";
        private final String ATTR_PATH = "path";
        private final String ATTR_NAME = "name";
        private final String ATTR_DATE = "date";

        @Override
        protected String doInBackground(String... urls) {
            NetworkService network = new NetworkService();
            HashMap<String, String> params = new HashMap<String, String>() {{
                put("id", patientElement.getId());
            }};
            return network.makeServiceCall(urls[0] + "/patient");
        }
        @Override
        protected void onPostExecute(String result) {
            TypeOfElement type;
            String name;
            String path;
            String date;

            try {
                if(!StringUtils.isEmpty(result)) {
                    patientDataList.clear();
                    JSONArray array = new JSONArray(result);
                    JSONObject element;

                    for (int i = 0; i < array.length(); i++) {
                        element = (JSONObject) array.get(i);
                        type = TypeOfElement.valueOf(element.optString(ATTR_TYPE));
                        name = element.optString(ATTR_NAME);
                        path = element.optString(ATTR_PATH);
                        date = element.optString(ATTR_DATE);

                        patientDataList.add(new PatientDataElement(name, type, path, date));
                    }

                    final PatientDataAdapter adapter = new PatientDataAdapter(getContext(), patientDataList);
                    listView.setAdapter(adapter);

                    Toast.makeText(getContext(), "Пацієнти знайдені", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Неможливо отримати список пацієнтів", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class PatientDataAdapter extends ArrayAdapter<PatientDataElement> {
        List<PatientDataElement> patientDataElements;
        Context context;

        public PatientDataAdapter(Context context, List<PatientDataElement> patientDataElements) {
            super(context, R.layout.patient_data_element, patientDataElements);

            this.patientDataElements = patientDataElements;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View patientDataView = inflater.inflate(R.layout.patient_data_element, parent, false);

            TextView fileNameView = (TextView) patientDataView.findViewById(R.id.fileName);
            TextView fileDateView = (TextView) patientDataView.findViewById(R.id.fileDate);
            ImageView icon = (ImageView) patientDataView.findViewById(R.id.icon);

            final PatientDataElement patientDataElement = patientDataElements.get(position);

            fileNameView.setText(patientDataElement.getName());
            fileDateView.setText(patientDataElement.getDate());
            if(patientDataElement.getType().equals(TypeOfElement.DCM)) {
                icon.setImageResource(R.drawable.ic_image_dcm);
            } else {
                icon.setImageResource(R.drawable.ic_file);
            }

            return patientDataView;
        }
    }

}
