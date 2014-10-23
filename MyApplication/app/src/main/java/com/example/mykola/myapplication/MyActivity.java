package com.example.mykola.myapplication;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.imebra.dicom.CodecFactory;
import com.imebra.dicom.ColorTransformsFactory;
import com.imebra.dicom.DataSet;
import com.imebra.dicom.DicomView;
import com.imebra.dicom.Image;
import com.imebra.dicom.ModalityVOILUT;
import com.imebra.dicom.Stream;
import com.imebra.dicom.StreamReader;
import com.imebra.dicom.TransformsChain;
import com.imebra.dicom.VOILUT;

import java.util.LinkedList;
import java.util.List;

// This activity is able to display a Dicom image
public class MyActivity extends Activity {
    private static final String FTYPE = ".dcm";
    private String mChosenFile;
    private DicomView dcm;

    private List<Image> images;
    private List<TransformsChain> chains;

    private int currentFrame = 0;
    private TextView vFrameNum;
    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        Button clickButton = (Button) findViewById(R.id.changeFrame);
        System.loadLibrary("imebra_lib");

        clickButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFrame++;
                if(currentFrame >= images.size())
                    currentFrame = 0;
                if(images.size() != 0) {
                    dcm.setImage(images.get(currentFrame), chains.get(currentFrame));
                }
            }
        });
    }

    // Called when the activity starts
    @Override
    public void onStart() {
        super.onStart();
    }

    private void loadDCM(String fileName) {
        // Open the dicom file from sdcard
        Stream stream = new Stream();
        stream.openFileRead(fileName);
        images = new LinkedList<Image>();
        chains = new LinkedList<TransformsChain>();

        // Build an internal representation of the Dicom file. Tags larger than 256 bytes
        //  will be loaded on demand from the file
        DataSet dataSet = CodecFactory.load(new StreamReader(stream), 256);

        try {
            for (int i = 0; ; i++) {
                images.add(dataSet.getImage(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int i = 0; i < images.size(); i++) {
            Image image = images.get(i);
            // Monochrome images may have a modality transform
            if (ColorTransformsFactory.isMonochrome(image.getColorSpace())) {
                ModalityVOILUT modalityVOILUT = new ModalityVOILUT(dataSet);
                if (!modalityVOILUT.isEmpty()) {
                    Image modalityImage = modalityVOILUT.allocateOutputImage(image, image.getSizeX(), image.getSizeY());
                    modalityVOILUT.runTransform(image, 0, 0, image.getSizeX(), image.getSizeY(), modalityImage, 0, 0);
                    images.set(i, modalityImage);
                }
            }
            // Allocate a transforms chain: contains all the transforms to execute before displaying
            //  an image
            TransformsChain transformsChain = new TransformsChain();
            // Monochromatic image may require a presentation transform to display interesting data
            if (ColorTransformsFactory.isMonochrome(image.getColorSpace())) {
                VOILUT voilut = new VOILUT(dataSet);

                int voilutId = voilut.getVOILUTId(0);
                if (voilutId != 0) {
                    voilut.setVOILUT(voilutId);
                } else {
                    // No presentation transform is present: here we calculate the optimal window/width (brightness,
                    //  contrast) and we will use that
                    voilut.applyOptimalVOI(image, 0, 0, image.getSizeX(), image.getSizeY());
                }
                transformsChain.addTransform(voilut);
            }
            chains.add(transformsChain);
        }

        // Let's find the DicomView and se the image
        if (dcm == null) {
            dcm = new DicomView(this);

            dcm.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.FILL_PARENT));
            dcm.setBackgroundColor(Color.BLACK);
            LinearLayout main = (LinearLayout) findViewById(R.id.dcmLayout);
            main.addView(dcm);
        }

        dcm.setImage(images.get(currentFrame), chains.get(currentFrame));
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