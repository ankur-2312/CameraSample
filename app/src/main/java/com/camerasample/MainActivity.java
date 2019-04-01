package com.camerasample;


import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    //Method to initialize and setting listener to the buttons
    private void init() {
        Button butTakePhoto = findViewById(R.id.butTakePhoto);
        Button butGetPhoto = findViewById(R.id.butGetPhoto);
        butTakePhoto.setOnClickListener(this);
        butGetPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            //Button for going to Take Photo activity
            case R.id.butTakePhoto:
                Intent cameraIntent = new Intent(MainActivity.this, TakePhotoActivity.class);
                startActivity(cameraIntent);
                break;

            //Button for going to Get Photo activity
            case R.id.butGetPhoto:
                Intent galleryIntent = new Intent(MainActivity.this, GetImageFromGalleryActivity.class);
                startActivity(galleryIntent);
                break;

            default:


        }
    }


}
