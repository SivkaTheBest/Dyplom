package com.example.mykola.mydicom;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.imebra.dicom.DicomView;

import uk.co.senab.photoview.PhotoViewAttacher;

// This activity is able to display a Dicom image
public class MyActivity extends Activity {

    private static final String FTYPE = ".dcm";
    ImageView img;
    PhotoViewAttacher mAttacher;

    private String mChosenFile;
    private DicomView dcm;
    private DCMData dcmData = new DCMData();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        Button brPlus = (Button) findViewById(R.id.brPlus);
        Button brMinus = (Button) findViewById(R.id.brMinus);

        Button cnPlus = (Button) findViewById(R.id.cnPlus);
        Button cnMinus = (Button) findViewById(R.id.cnMinus);

        Button inverse = (Button) findViewById(R.id.inverse);
        System.loadLibrary("imebra_lib");

        img = (ImageView) findViewById(R.id.image);

        cnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dcmData.addContrast(0.1);
                redrawImage();
            }
        });

        cnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dcmData.minusContrast(0.1);
                redrawImage();
            }
        });

        brPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dcmData.addBrightness(10);
                redrawImage();
            }
        });

        brMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dcmData.minusBrightness(10);
                redrawImage();
            }
        });

        inverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dcmData.inverse();
                redrawImage();
            }
        });

        mAttacher = new PhotoViewAttacher(img);
    }

    private void redrawImage() {
        if(dcmData.isLoaded()) {
            img.setImageBitmap(dcmData.getFrame(0));
        }
    }

    // Called when the activity starts
    @Override
    public void onStart() {
        super.onStart();
    }

    private void loadDCM(String fileName) {
        dcmData.loadDCM(fileName);
        ImageView img = (ImageView) findViewById(R.id.image);
        img.setImageBitmap(dcmData.getFrame(0));
        mAttacher.update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                OpenFileDialog fileDialog = new OpenFileDialog(this).setFilter(FTYPE).setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                    @Override
                    public void OnSelectedFile(String fileName) {
                        mChosenFile = fileName;
                        setTitle(mChosenFile);
                        Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_LONG).show();
                        loadDCM(fileName);
                    }
                });
                fileDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}