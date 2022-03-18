package com.example.cameraview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class PictureActivity extends AppCompatActivity {
    private ImageView picture;
    private ImageButton deletePicture, savePicture;
    private Uri uri;
    private int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        picture = findViewById(R.id.picture);
        deletePicture = findViewById(R.id.deletePicture);
        savePicture = findViewById(R.id.savePicture);

        ActivityCompat.requestPermissions(PictureActivity.this
                , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        ActivityCompat.requestPermissions(PictureActivity.this
                , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        Intent intent = getIntent();
        byte[] dataPicture = intent.getByteArrayExtra("picture");
        if (dataPicture != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(dataPicture, 0, dataPicture.length);
            picture.setImageBitmap(bitmap);
        } else {
            uri = Uri.parse(getIntent().getStringExtra("chooserPicture"));
            picture.setImageURI(uri);
        }

        deletePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uri != null) {
                    if (deltefolderwithimages(new File(UriUtils.getPathFromUri(getApplicationContext(), uri)))) {
                        Toast.makeText(PictureActivity.this, "Picture was not deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PictureActivity.this, "Picture was deleted", Toast.LENGTH_SHORT).show();
                    }
                    onBackPressed();
                } else {
                    Toast.makeText(PictureActivity.this, "Picture was deleted", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        });

        savePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToGallery();
                Toast.makeText(PictureActivity.this, "Picture was saved", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
    }

    public boolean deltefolderwithimages(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deltefolderwithimages(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        if (dir.delete()) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(dir);
            mediaScanIntent.setData(contentUri);
            PictureActivity.this.sendBroadcast(mediaScanIntent);
        }
        return dir.delete();
    }

    private void saveToGallery() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) picture.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        FileOutputStream outputStream = null;
        File file = Environment.getExternalStorageDirectory();
        File dir = new File(file.getAbsolutePath() + "/MyPicture");
        dir.mkdirs();

        String filename = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, filename);
        try {
            outputStream = new FileOutputStream(outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        try {
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(outFile);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}