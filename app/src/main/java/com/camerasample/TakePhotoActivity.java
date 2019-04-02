package com.camerasample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class TakePhotoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_REQUEST_CODE_1 = 100;
    private static final int PERMISSION_REQUEST_CODE_2 = 200;
    private static final int CAMERA_REQUEST_CODE = 300;
    private ImageView ivImage;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        init();
    }

    //Method to initialize and setting listener to the buttons
    private void init() {
        Button butTakePhotoPrivate = findViewById(R.id.butTakePhotoPrivate);
        Button butTakePhotoPublic = findViewById(R.id.butTakePhotoPublic);
        ivImage = findViewById(R.id.ivImage);
        butTakePhotoPrivate.setOnClickListener(this);
        butTakePhotoPublic.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            //Button for save photo in private external storage
            case R.id.butTakePhotoPrivate:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE_1);
                    } else {
                        dispatchTakePictureIntent(1);
                    }
                } else {
                    dispatchTakePictureIntent(1);
                }
                break;

            //Button for save photo in public external storage
            case R.id.butTakePhotoPublic:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String[] str = checkPermissionPublic();
                    if (str.length == 0) {
                        dispatchTakePictureIntent(2);
                    } else {

                        ActivityCompat.requestPermissions(this, str, PERMISSION_REQUEST_CODE_2);
                    }
                } else {

                    dispatchTakePictureIntent(2);
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_1:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent(1);
                } else {
                    int flag = 0;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            flag++;
                            openSettingsForPermission();
                        }
                    }
                    if (flag == 0) {
                        givePrivateExternalPermissionsDetailsToUser();
                    }

                }
                break;

            case PERMISSION_REQUEST_CODE_2:

                int deniedCount = 0;
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        deniedCount++;
                    }
                }

                if (deniedCount == 0) {
                    dispatchTakePictureIntent(2);
                } else {

                    String str[] = checkPermissionPublic();
                    int flag = 0;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        for (String aStr : str) {
                            if (!shouldShowRequestPermissionRationale(aStr)) {
                                flag++;
                                openSettingsForPermission();
                                break;
                            }
                        }
                    }
                    if (flag == 0) {
                        givePublicExternalPermissionsDetailsToUser();
                    }
                }
                break;
        }
    }

    //This method get callback when other activity is started with startActivityForResult
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            Glide.with(this).load(currentPhotoPath).centerCrop().into(ivImage);
            galleryAddPic();
           // deleteLastPhotoTaken();

        }
    }

    //This method create image file path for private external location
    private File createImageFilePrivate() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat(getString(R.string.yyyyMMdd_HHmmss)).format(new Date());
        String imageFileName = getString(R.string.JPEG_) + timeStamp + getString(R.string.specialCharacter);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                getString(R.string.imageExtension),
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //This method create image file path for public external location
    private File createImageFilePublic() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat(getString(R.string.yyyyMMdd_HHmmss)).format(new Date());
        String imageFileName = getString(R.string.JPEG_) + timeStamp + getString(R.string.specialCharacter);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                getString(R.string.imageExtension),
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //This method fire the intent for start the camera activity
    private void dispatchTakePictureIntent(int check) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            if (check == 1) {
                try {
                    photoFile = createImageFilePrivate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    photoFile = createImageFilePublic();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getString(R.string.com_example_android_fileprovider),
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    //This method scan all images in the public external storage in the gallery
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    //This method check for runtime permissions
    private String[] checkPermissionPublic() {
        ArrayList<String> listPermission = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                listPermission.add(Manifest.permission.CAMERA);
            }

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                listPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        String[] str = new String[listPermission.size()];
        for (int i = 0; i < listPermission.size(); i++) {
            str[i] = listPermission.get(i);

        }

        return str;
    }

    //This method shows the dialog box to go to settings
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(TakePhotoActivity.this)
                .setMessage(message)
                .setPositiveButton(R.string.ok, okListener)
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    //Method to open settings so that user can give permission
    private void openSettingsForPermission() {
        showMessageOKCancel(getString(R.string.go_to_settings),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent viewIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                        startActivity(viewIntent);
                    }
                });
    }

    //Method to give permission requirement to the user
    private void givePublicExternalPermissionsDetailsToUser() {
        showMessageOKCancel(getString(R.string.permissionRequirement),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] str = checkPermissionPublic();
                        if (str.length != 0) {
                            ActivityCompat.requestPermissions(TakePhotoActivity.this, str, PERMISSION_REQUEST_CODE_2);
                        }
                    }
                });
    }

    //Method to give permission requirement to the user
    private void givePrivateExternalPermissionsDetailsToUser() {
        showMessageOKCancel(getString(R.string.permissionRequirement),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(TakePhotoActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE_1);

                    }
                });
    }
    private void deleteLastPhotoTaken() {

        String[] projection = new String[] {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE };

        final Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                null,null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        if (cursor != null) {
            cursor.moveToFirst();

            int column_index_data =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            String image_path = cursor.getString(column_index_data);
            cursor.close();

            File file = new File(image_path);
            if (file.exists()) {
                file.delete();
            }
        }
    }

}

