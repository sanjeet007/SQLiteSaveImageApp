package com.ezimax.sqlitesaveimageapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "StoreImageActivity";

    private Button btnOpenGallery, btnSaveImage, btnLoadImage;
    private TextView tvStatus;
    private AppCompatImageView imgView, imgLoaded;
    private Uri selectedImageUri = null;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenGallery = findViewById(R.id.btnSelectImage);
        btnSaveImage = findViewById(R.id.btnSaveImage);
        btnLoadImage = findViewById(R.id.btnLoadImage);
        imgLoaded = findViewById(R.id.loadedImg);
        imgView = findViewById(R.id.imgView);
        tvStatus = findViewById(R.id.tvStatus);

        btnOpenGallery.setOnClickListener(this);
        btnSaveImage.setOnClickListener(this);
        btnLoadImage.setOnClickListener(this);

        // Create the Database helper object
        dbHelper = new DBHelper(this);
    }

    void showMessage(final String message) {
        tvStatus.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setAlpha(0);
                tvStatus.animate().alpha(1).setDuration(500);
                tvStatus.setText(message);

            }
        });
    }

    // Choose an image from Gallery
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    imgView.setImageURI(selectedImageUri);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnOpenGallery)
            openImageChooser();

        if (v == btnSaveImage) {
            // Saving to Database...
            if (selectedImageUri!=null) {
                saveImageInDB();
                showMessage("Image Saved in Database...");
            }
        }
        if (v == btnLoadImage)
            loadImageFromDB();
    }

    boolean saveImageInDB() {

        try {
            dbHelper.open();
            InputStream iStream = getContentResolver().openInputStream(selectedImageUri);
            byte[] inputData = Utils.getBytes(iStream);
            dbHelper.insertImage(inputData);
            dbHelper.close();
            return true;
        } catch (IOException ioe) {
            Log.e(TAG, "<saveImageInDB> Error : " + ioe.getLocalizedMessage());
            dbHelper.close();
            return false;
        }

    }

    void loadImageFromDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dbHelper.open();
                    final byte[] bytes = dbHelper.retreiveImageFromDB();
                    dbHelper.close();
                    // Show Image from DB in ImageView
                    imgLoaded.post(new Runnable() {
                        @Override
                        public void run() {
                            imgLoaded.setImageBitmap(Utils.getImage(bytes));
                            showMessage("Image Loaded from Database");
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "<loadImageFromDB> Error : " + e.getLocalizedMessage());
                    dbHelper.close();
                }
            }
        }).start();
    }
}
