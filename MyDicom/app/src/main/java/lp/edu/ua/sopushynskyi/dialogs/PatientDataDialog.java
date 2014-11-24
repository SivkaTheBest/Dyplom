package lp.edu.ua.sopushynskyi.dialogs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import lp.edu.ua.sopushynskyi.dialogs.beans.PatientDataElement;
import lp.edu.ua.sopushynskyi.dialogs.beans.PatientElement;
import lp.edu.ua.sopushynskyi.dialogs.beans.TypeOfElement;
import lp.edu.ua.sopushynskyi.dicom.NetworkService;

public class PatientDataDialog extends AlertDialog.Builder {

    private PatientElement patientElement;
    private List<PatientDataElement> patientDataList = new LinkedList<PatientDataElement>();
    private ListView listView;
    private String url;
    private Integer selectedIndex = -1;
    private PatientDataAdapter adapter;
    private ProgressDialog mProgressDialog;
    private DialogListener listener;

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
                .setNeutralButton(R.string.refresh, null)
                .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GetPatientImageTask();
                    }
                });

        listView = (ListView) view.findViewById(R.id.patientsData);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final ArrayAdapter<PatientDataElement> adapter = (PatientDataAdapter) adapterView.getAdapter();
                selectedIndex = i;
                adapter.notifyDataSetChanged();
            }
        });

        GetPatientDataListTask();
    }

    @Override
    public AlertDialog show() {
        AlertDialog dialog = super.show();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetPatientDataListTask();
            }
        });
        return dialog;
    }

    private void GetPatientDataListTask() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetPatientDataListTask().execute(url);
        } else {
            Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
        }
    }

    private void GetPatientImageTask() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            new GetPatientImageTask().execute(url);
        } else {
            Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage(getContext().getString(R.string.downloading));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    public void setListener(DialogListener listener) {
        this.listener = listener;
    }

    private class GetPatientImageTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected String doInBackground(String... urls) {
            int count;

            try {
                URL url = new URL(urls[0] + String.format("/image?id=%s&filePath=%s",
                        patientElement.getId(), patientDataList.get(selectedIndex).getName()));
                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lenghtOfFile = conexion.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory()
                        + "/" + patientDataList.get(selectedIndex).getName());

                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    Integer progress = (int) ((total * 100) / lenghtOfFile);
                    publishProgress(progress);
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgressDialog.setProgress((progress[0]));
        }

        @Override
        protected void onPostExecute(String entity) {
            listener.OnSelectedResult(Environment.getExternalStorageDirectory()
                    + "/" + patientDataList.get(selectedIndex).getName());
            mProgressDialog.dismiss();
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
            return network.makeServiceCall(urls[0] + "/patient", params);
        }

        @Override
        protected void onPostExecute(String result) {
            TypeOfElement type;
            String name;
            String path;
            String date;

            try {
                if (!StringUtils.isEmpty(result)) {
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

                    adapter = new PatientDataAdapter(getContext(), patientDataList);
                    listView.setAdapter(adapter);

                    Toast.makeText(getContext(), R.string.data_received, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.cant_get_patient_data, Toast.LENGTH_SHORT).show();
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
            if (patientDataElement.getType().equals(TypeOfElement.DCM)) {
                icon.setImageResource(R.drawable.ic_image_dcm);
            } else {
                icon.setImageResource(R.drawable.ic_file);
            }

            if (selectedIndex == position) {
                patientDataView.setBackgroundColor(getContext().getResources().getColor(android.R.color.secondary_text_dark));
            } else {
                patientDataView.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
            }

            return patientDataView;
        }
    }

}
