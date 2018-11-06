package com.addict.takepicturecamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.net.Uri;
import java.io.File;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.*;
import android.graphics.drawable.Drawable;
import 	android.graphics.drawable.BitmapDrawable;



public class MainActivity extends Activity {

    static int TAKE_PIC =1;
    Uri outPutfileUri;
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image);
    }


    public void onClick(View view) {

        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(),
                "MyPhoto.jpg");
        outPutfileUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutfileUri);
        startActivityForResult(intent, TAKE_PIC);
    }

    Bitmap bitmap = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data)
    {
        if (requestCode == TAKE_PIC && resultCode==RESULT_OK) {

            String uri = outPutfileUri.toString();
            Log.e("uri-:", uri);
            Toast.makeText(this, outPutfileUri.toString(),Toast.LENGTH_LONG).show();

            //Bitmap myBitmap = BitmapFactory.decodeFile(uri);
            // mImageView.setImageURI(Uri.parse(uri));   OR drawable make image strechable so try bleow also

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), outPutfileUri);
                Drawable d = new BitmapDrawable(getResources(), bitmap);
                mImageView.setImageDrawable(d);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

}
