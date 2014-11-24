package lp.edu.ua.sopushynskyi.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mykola.mydicom.R;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class OpenFileDialog extends AlertDialog.Builder {

    private String currentPath = Environment.getExternalStorageDirectory().getPath();
    private List<File> files = new ArrayList<File>();
    private TextView title;
    private ListView listView;
    private FilenameFilter filenameFilter;
    private int selectedIndex = -1;
    private OpenDialogListener listener;
    public static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public OpenFileDialog(Context context) {
        super(context);
        setCancelable(true);

        title = createTitle(context);
        changeTitle();
        LinearLayout linearLayout = createMainLayout(context);
        files.addAll(getFiles(currentPath));
        listView = createListView(context);
        listView.setAdapter(new FileAdapter(context, files));

        linearLayout.addView(listView);
        setCustomTitle(title)
                .setView(linearLayout)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedIndex > -1 && listener != null) {
                            listener.OnSelectedFile(listView.getItemAtPosition(selectedIndex).toString());
                        }
                    }
                });
    }

    private static Display getDefaultDisplay(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    private static Point getScreenSize(Context context) {
        Point screeSize = new Point();
        getDefaultDisplay(context).getSize(screeSize);
        return screeSize;
    }

    private static int getLinearLayoutMinHeight(Context context) {
        return getScreenSize(context).y;
    }

    @Override
    public AlertDialog show() {
        files.addAll(getFiles(currentPath));
        listView.setAdapter(new FileAdapter(getContext(), files));
        return super.show();
    }

    public OpenFileDialog setFilter(final String filter) {
        filenameFilter = new FilenameFilter() {

            @Override
            public boolean accept(File file, String fileName) {
                File tempFile = new File(String.format("%s/%s", file.getPath(), fileName));
                if (tempFile.isFile())
                    return tempFile.getName().endsWith(filter);
                return true;
            }
        };
        return this;
    }

    private TextView createTextView(Context context, int style) {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, style);
        int itemHeight = getItemHeight(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
        textView.setMinHeight(itemHeight);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(15, 0, 0, 0);
        return textView;
    }

    private TextView createTitle(Context context) {
        TextView textView = createTextView(context, android.R.style.TextAppearance_DeviceDefault_DialogWindowTitle);
        textView.setPadding(40, 40, 40, 40);
        return textView;
    }

    public int getTextWidth(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.left + bounds.width() + 80;
    }

    private void changeTitle() {
        String titleText = currentPath;
        int screenWidth = getScreenSize(getContext()).x;
        int maxWidth = (int) (screenWidth * 0.99);
        if (getTextWidth(titleText, title.getPaint()) > maxWidth) {
            while (getTextWidth("..." + titleText, title.getPaint()) > maxWidth) {
                int start = titleText.indexOf("/", 2);
                if (start > 0)
                    titleText = titleText.substring(start);
                else
                    titleText = titleText.substring(2);
            }
            title.setText("..." + titleText);
        } else {
            title.setText(titleText);
        }
    }

    private int getItemHeight(Context context) {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getTheme().resolveAttribute(android.R.attr.rowHeight, value, true);
        getDefaultDisplay(context).getMetrics(metrics);
        return (int) TypedValue.complexToDimension(value.data, metrics);
    }

    private void RebuildFiles(ArrayAdapter<File> adapter) {
        try {
            selectedIndex = -1;
            List<File> fileList = getFiles(currentPath);
            files.clear();
            files.addAll(fileList);
            adapter.notifyDataSetChanged();
            changeTitle();
        } catch (NullPointerException e) {
            Toast.makeText(getContext(), android.R.string.unknownName, Toast.LENGTH_SHORT).show();
        }
    }

    private LinearLayout createMainLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setMinimumHeight(getLinearLayoutMinHeight(context));
        return linearLayout;
    }

    private List<File> getFiles(String directoryPath) {
        File directory = new File(directoryPath);
        List<File> fileList = new LinkedList<File>(Arrays.asList(directory.listFiles(filenameFilter)));

        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File file, File file2) {
                if (file.isDirectory() && file2.isFile())
                    return -1;
                else if (file.isFile() && file2.isDirectory())
                    return 1;
                else
                    return file.getPath().compareTo(file2.getPath());
            }
        });

        if (!currentPath.equals(Environment.getExternalStorageDirectory().getPath())) {
            fileList.add(0, new File(currentPath).getParentFile());
        }

        return fileList;
    }

    private ListView createListView(Context context) {
        ListView listView = new ListView(context);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                final ArrayAdapter<File> adapter = (FileAdapter) adapterView.getAdapter();
                File file = adapter.getItem(index);
                selectedIndex = -1;
                if (file.isDirectory()) {
                    currentPath = file.getPath();
                    RebuildFiles(adapter);
                } else {
                    if (index != selectedIndex)
                        selectedIndex = index;
                }

                adapter.notifyDataSetChanged();
            }
        });
        return listView;
    }

    public OpenFileDialog setOpenDialogListener(OpenDialogListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OpenDialogListener {
        public void OnSelectedFile(String fileName);
    }

    private class FileAdapter extends ArrayAdapter<File> {

        public FileAdapter(Context context, List<File> files) {
            super(context, R.layout.patient_data_element, files);
        }

        private LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View fileElementView = inflater.inflate(R.layout.patient_data_element, parent, false);
            TextView fileNameView = (TextView) fileElementView.findViewById(R.id.fileName);
            TextView fileDateView = (TextView) fileElementView.findViewById(R.id.fileDate);
            ImageView icon = (ImageView) fileElementView.findViewById(R.id.icon);

            File file = getItem(position);
            String filePath = file.getAbsolutePath();
            String parentFilePath = new File(currentPath).getParentFile().getAbsolutePath();
            String fileDate = sdf.format(file.lastModified());
            boolean isDirectory = file.isDirectory();

            if (StringUtils.equals(filePath, parentFilePath)) {
                fileNameView.setText(R.string.up);
                fileDateView.setText(R.string.up_text);
                icon.setImageResource(R.drawable.ic_up);
            } else {
                fileNameView.setText(file.getName());
                if(isDirectory) {
                    fileDateView.setText(R.string.folder);
                    icon.setImageResource(R.drawable.ic_folder);
                } else {
                    fileDateView.setText(fileDate);
                    icon.setImageResource(R.drawable.ic_image_dcm);
                }
            }

            if (selectedIndex == position) {
                fileElementView.setBackgroundColor(getContext().getResources().getColor(android.R.color.secondary_text_dark));
            } else {
                fileElementView.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
            }
            return fileElementView;
        }
    }
}