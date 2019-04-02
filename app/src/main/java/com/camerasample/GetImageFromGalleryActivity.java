package com.camerasample;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;

import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class GetImageFromGalleryActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST_CODE_1 = 300;
    private static final int GALLERY_REQUEST_CODE = 200;
    private static final int GALLERY_REQUEST_CODE_1 = 400;
    private ImageDataAdapter adapter;
    private ArrayList<Bitmap> uriList;

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_image_from_gallery);
        init();
    }

    //Method to initialize and setting listener to the buttons and set recycler view with adapter
    private void init() {
        Button butGetSingleImage = findViewById(R.id.butGetSingleImage);
        Button butGetMultipleImage = findViewById(R.id.butGetMultipleImage);
        RecyclerView recyclerview = findViewById(R.id.rv);
        uriList = new ArrayList<>();
        adapter = new ImageDataAdapter(uriList);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setAdapter(adapter);
        butGetSingleImage.setOnClickListener(this);
        butGetMultipleImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            //Button to pick single image from gallery
            case R.id.butGetSingleImage:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    } else {
                        pickFromGallerySingle();
                    }

                } else {
                    pickFromGallerySingle();

                }
                break;

            ////Button to pick Multiple image from gallery
            case R.id.butGetMultipleImage:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_1);
                    } else {
                        pickFromGalleryMultiple();
                    }

                } else {
                    pickFromGalleryMultiple();

                }
                break;
        }

    }

    //This method get callback when other activity is started with startActivityForResult
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:

                    Uri uri = data.getData();
                    Bitmap bitmapafterCheckRotation = null;
                    try {
                        bitmapafterCheckRotation = rotateImageIfRequired(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri), uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    uriList.clear();
                    uriList.add(bitmapafterCheckRotation);
                    adapter.notifyDataSetChanged();
                    break;

                case GALLERY_REQUEST_CODE_1:

                    if (data.getClipData() != null) {
                        uriList.clear();

                        ClipData mClipData = data.getClipData();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uriMulti = item.getUri();
                            Bitmap bitmapafterCheckRotationMulti = null;
                            try {
                                bitmapafterCheckRotationMulti = rotateImageIfRequired(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriMulti), uriMulti);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            uriList.add(bitmapafterCheckRotationMulti);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    break;

                default:
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallerySingle();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            openSettingsForPermission();
                        } else {
                            giveSingleImagePermissionsDetailsToUser();
                        }
                    }
                }
                break;

            case PERMISSION_REQUEST_CODE_1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGalleryMultiple();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            openSettingsForPermission();
                        } else {
                            giveMultipleImagePermissionsDetailsToUser();
                        }
                    }
                }
                break;

            default:

        }
    }

    //Method to pick single image from gallery
    private void pickFromGallerySingle() {
        Intent captureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(captureIntent, GALLERY_REQUEST_CODE);
    }

    //Method to pick multiple image from gallery
    private void pickFromGalleryMultiple() {
        Intent captureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        captureIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(captureIntent, GALLERY_REQUEST_CODE_1);
    }

    //This method shows the dialog box to go to settings
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(GetImageFromGalleryActivity.this)
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
    private void giveSingleImagePermissionsDetailsToUser() {
        showMessageOKCancel(getString(R.string.permissionRequirement),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                        }

                    }
                });
    }

    //Method to give permission requirement to the user
    private void giveMultipleImagePermissionsDetailsToUser() {
        showMessageOKCancel(getString(R.string.permissionRequirement),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_1);
                        }

                    }
                });
    }

    private Bitmap rotateImageIfRequired(Bitmap bitmap, Uri uri) throws IOException {

        InputStream input = this.getContentResolver().openInputStream(uri);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23) {
            assert input != null;
            ei = new ExifInterface(input);
        } else
            ei = new ExifInterface(Objects.requireNonNull(uri.getPath()));

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);
            default:
                return bitmap;
        }
    }
}

