package com.addict.androidcamera;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.ColorSpace;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    ImageView ivImage;
    Integer REQUEST_CAMERA=1,SAVE_IMAGE=2,GRAY_SCALE=3,OPEN_GALLERY=4;
    Bitmap imageBitmap;
    Bitmap grayBitmap;
    Bitmap noiseBitmap;
    Uri imageUri;
    private Mat GaussianNoise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ivImage =(ImageView)findViewById(R.id.ivImage);

        OpenCVLoader.initDebug();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                SelectImage();

            }
        });
    }

    private void SelectImage() {

        final CharSequence[] items = {"Camera", "Gallery", "Save","GreyScale","AddNoise"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Functions");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);


                } else if (items[i].equals("Gallery")) {

                    openGallery(ivImage);

                   /* Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Select File"), SELECT_FILE);*/


                } else if (items[i].equals("Save")) {


                    savePhotoToMySdCard(imageBitmap);

                    Toast.makeText(getApplicationContext(), "Photo has been saved to SD card!", Toast.LENGTH_SHORT).show();


                }else if (items[i].equals("GreyScale")) {

                    convertToGray(ivImage);

                }else if (items[i].equals("AddNoise")) {

                    addGaussianNoise(ivImage);

                }
            }
        });
        builder.show();
    }
    private void savePhotoToMySdCard(Bitmap bit){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
        String pname = sdf.format(new Date());
        String root = Environment.getExternalStorageDirectory().toString();
        File folder = new File(root+"/SCC_Photos");
        folder.mkdirs();
        File my_file = new File(folder, pname+".png");
        try {
            FileOutputStream stream = new FileOutputStream(my_file);
            bit.compress(Bitmap.CompressFormat.PNG, 80, stream);
            stream.flush();
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openGallery (View v){

        Intent myIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(myIntent,4);
    }

    public  void convertToGray(View v)
    {
        Mat Rgba = new Mat();
        Mat grayMat = new Mat();
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inDither=false;
        o.inSampleSize=4;
        int width = imageBitmap.getWidth();
        int height= imageBitmap.getHeight();
        grayBitmap=Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);
        //bitmap to MAT
        Utils.bitmapToMat(imageBitmap,Rgba);
        Imgproc.cvtColor(Rgba,grayMat,Imgproc.COLOR_RGB2GRAY);
        Utils.matToBitmap(grayMat,grayBitmap);
        ivImage.setImageBitmap(grayBitmap);
    }


    public void addGaussianNoise(View v){

        int variance=0;
        double mean=0.05;
        Mat grayMat = new Mat();
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inDither=false;
        o.inSampleSize=1;
        int width = grayBitmap.getWidth();
        int height= grayBitmap.getHeight();
        noiseBitmap=Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);
        //bitmap to MAT
        Utils.bitmapToMat(grayBitmap,grayMat);
        Mat noiseMat = new Mat(grayMat.rows(), grayMat.cols(), grayMat.type());
        java.util.Random r = new java.util.Random();
        double noise = r.nextGaussian() * Math.sqrt(variance) + mean;
        noiseMat.setTo(new Scalar(noise, noise, noise));
        Core.add(grayMat,noiseMat,grayMat);
        Utils.matToBitmap(grayMat,noiseBitmap);
        ivImage.setImageBitmap(noiseBitmap);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){

        super.onActivityResult(requestCode,resultCode,data);

        //Did the user choose OK? IF so , the code inside these curly braces will execute.
        if(resultCode== Activity.RESULT_OK){

            if(requestCode==REQUEST_CAMERA){
                //we are hearing back from the camera.

               Bundle bundle = data.getExtras();
                imageBitmap = (Bitmap) bundle.get("data");

                //at this point,we have the image from the camera.
                ivImage.setImageBitmap(imageBitmap);

            }else if (requestCode==OPEN_GALLERY){

                imageUri=data.getData();
               /* Uri selectedImageUri =data.getData();
                ivImage.setImageURI(selectedImageUri);*/
                try{
                    imageBitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
               ivImage.setImageBitmap(imageBitmap);

            }else if (requestCode==SAVE_IMAGE){

            }else if (requestCode==GRAY_SCALE){



            }}

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}