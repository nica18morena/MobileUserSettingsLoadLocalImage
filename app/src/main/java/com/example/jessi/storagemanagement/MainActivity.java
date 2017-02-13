package com.example.jessi.storagemanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int RC_PHOTO_PICKER =  2;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button mPhotoPickerButton = (Button) findViewById(R.id.button_load_media);
        //Button mPhotoTakerButton = (Button) findViewById(R.id.button_take_media);
        //TODO: create default setup to show both buttons, create methods that call to create buttons
        sharedPreferencesSetup();
    }

    private void sharedPreferencesSetup(){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        this.photoPickerOnClickListener();

        boolean photoTakerButtonVisible = sharedPreferences.getBoolean(getString(R.string.show_take_pic_key),
                getResources().getBoolean(R.bool.pref_show_take_pic_default));
        this.showPhotoTaker(photoTakerButtonVisible);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void showPhotoTaker(boolean isPhotoTakerVisible){
        Button mPhotoTakerButton = (Button) findViewById(R.id.button_take_media);

        if (isPhotoTakerVisible){
            mPhotoTakerButton.setVisibility(View.VISIBLE);
            this.takePhotoOnClickListener();
        }
        else {
            mPhotoTakerButton.setVisibility(View.GONE);
        }
    }

    private void photoPickerOnClickListener() {
        Button mPhotoPickerButton = (Button) findViewById(R.id.button_load_media);

        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent,
                        "Complete action using"), RC_PHOTO_PICKER);
            }
        });

    }

    private void takePhotoOnClickListener() {
        Button mPhotoTakerButton = (Button) findViewById(R.id.button_take_media);

        mPhotoTakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        ImageView ivCamera = (ImageView) findViewById(R.id.iv_viewImage);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivCamera.setImageBitmap(imageBitmap);
        }
        else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri image = data.getData();
            try{
                Bitmap imageBitmap = decodeBitmap(image);
                ivCamera.setImageBitmap(imageBitmap);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    public  Bitmap decodeBitmap(Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        final int REQUIRED_SIZE = 100;

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver()
                .openInputStream(selectedImage), null, o2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.show_take_pic_key))){

            boolean showButton = sharedPreferences.getBoolean(key, getResources()
                    .getBoolean((R.bool.pref_show_take_pic_default)));
            this.showPhotoTaker(showButton);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
