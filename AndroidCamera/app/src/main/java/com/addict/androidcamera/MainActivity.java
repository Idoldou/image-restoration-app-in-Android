package com.addict.androidcamera;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.ColorSpace;
import android.media.audiofx.NoiseSuppressor;
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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.lang.StrictMath;


public class MainActivity extends AppCompatActivity {

    ImageView ivImage;
    Integer REQUEST_CAMERA=1,SAVE_IMAGE=2,GRAY_SCALE=3,OPEN_GALLERY=4;
    Bitmap imageBitmap;
    Bitmap grayBitmap;
    Bitmap noiseBitmap;
    Bitmap medianBitmap;
    Bitmap meanBitmap;
    Bitmap GaussianblurBitmap;
    Bitmap BilateralFilterBitmap;
    Bitmap WienerBitmap;
    Bitmap BlocksBitmap;
    Uri imageUri;
    double psnr;


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

        final CharSequence[] items = {"Camera", "Gallery", "Save","GreyScale","AddNoise","MeanFilter","MedianFilter","GaussianBlur","BilateralFilter","WienerFilter","AddBlackBlocks","Inpainting"};
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

                }else if (items[i].equals("MedianFilter")) {

                    MedianFilter(ivImage);
                    psnr=MedianPSNR();
                    Toast.makeText(getApplicationContext(), "You are using Median Filter.The value of PSNR is :"+String.valueOf(psnr), Toast.LENGTH_LONG).show();

                }
                else if (items[i].equals("MeanFilter")){

                    MeanFilter(ivImage);
                    psnr=MeanPSNR();
                    Toast.makeText(getApplicationContext(), "You are using Mean Filter.The value of PSNR is :"+String.valueOf(psnr), Toast.LENGTH_LONG).show();

                }else if (items[i].equals("GaussianBlur")){

                    Gaussianblur(ivImage);
                    psnr = GausssianPSNR();
                    Toast.makeText(getApplicationContext(), "You are using Gaussian Filter.The value of PSNR is :"+String.valueOf(psnr), Toast.LENGTH_LONG).show();

                }else if (items[i].equals("BilateralFilter")) {

                    BilateralFilter(ivImage);
                    psnr = BilateralPSNR();
                    Toast.makeText(getApplicationContext(), "You are using Bilateral Filter.The value of PSNR is :"+String.valueOf(psnr), Toast.LENGTH_LONG).show();
                }else if (items[i].equals("WienerFilter")) {
                    WienerFilter(ivImage);
                }else if (items[i].equals("AddBlackBlocks")) {
                    AddBlocks(ivImage);
                }else if (items[i].equals("Inpainting")) {
                    Inpaint(ivImage);
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

    public  void convertToGray(View v) {
        Mat Rgba = new Mat();
        Mat grayMat = new Mat();
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inDither=false;
        o.inSampleSize=4;
        int width = imageBitmap.getWidth();
        int height= imageBitmap.getHeight();
        grayBitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        //bitmap to MAT
        Utils.bitmapToMat(imageBitmap,Rgba);
        Rgba.convertTo(Rgba,CvType.CV_8U);
        Imgproc.cvtColor(Rgba,grayMat,Imgproc.COLOR_BGR2GRAY);
        Utils.matToBitmap(grayMat,grayBitmap);
        ivImage.setImageBitmap(grayBitmap);
    }
    public  void AddBlocks(View v){
        Mat gray = new Mat();
        Utils.bitmapToMat(imageBitmap,gray);
        Imgproc.cvtColor(gray,gray,Imgproc.COLOR_BGRA2GRAY);
        BlocksBitmap = imageBitmap.copy(Bitmap.Config.RGB_565,true);
        Mat blocks = new Mat(gray.size(),gray.type());
        Core.randu(blocks,0,255);
        /*blocks.setTo(0,blocks<0.3);
        blocks.setTo(1,blocks>0.3);*/
        Imgproc.threshold(blocks,blocks,77,1,Imgproc.THRESH_BINARY);
        Core.multiply(gray,blocks,gray);
        Utils.matToBitmap(gray,BlocksBitmap);
        ivImage.setImageBitmap(BlocksBitmap);
    }


    public void addGaussianNoise(View v){
        Mat img = new Mat();
        Utils.bitmapToMat(grayBitmap,img);
        noiseBitmap = grayBitmap.copy(Bitmap.Config.RGB_565,true);
        Mat noiseMat = new Mat(img.size(), img.type());
        Core.randn(noiseMat,0,0.5);
        Core.normalize(noiseMat,noiseMat,0,255,Core.NORM_MINMAX);
        Core.add(img,noiseMat,img);
        Core.normalize(img,img,0,255,Core.NORM_MINMAX);
        Utils.matToBitmap(img,noiseBitmap);
        ivImage.setImageBitmap(noiseBitmap);
    }

    public void MedianFilter(View v){
        Mat img = new Mat();
        Utils.bitmapToMat(noiseBitmap,img);
        medianBitmap = noiseBitmap.copy(Bitmap.Config.RGB_565,true);
        Mat median = new  Mat(img.size(),img.type());
        Imgproc.medianBlur(img,median,7);
        Utils.matToBitmap(median,medianBitmap);
        ivImage.setImageBitmap(medianBitmap);
    }

    public double MedianPSNR() {
        Mat GrayImg = new Mat();
        Utils.bitmapToMat(grayBitmap, GrayImg);
        Mat MedianImg = new Mat();
        Utils.bitmapToMat(medianBitmap, MedianImg);
        Mat dst = new Mat();
        Core.absdiff(GrayImg, MedianImg, dst);
        dst.convertTo(dst, CvType.CV_32F);
        Core.multiply(dst, dst, dst);
        Scalar s =  Core.sumElems(dst);
        double sse = s.val[0] + s.val[1] + s.val[2];
        if (sse <= 1e-10)
            return 0;
        else
            {
            double mse = sse / (double) (GrayImg.channels() * GrayImg.total());
            double psnr = 10 * Math.log10 ((255 * 255) / mse);
            return psnr;
             }
    }

    public void MeanFilter(View v){
        Mat img = new Mat();
        Utils.bitmapToMat(noiseBitmap,img);
        meanBitmap=noiseBitmap.copy(Bitmap.Config.RGB_565,true);
        Mat mean = new  Mat(img.size(),img.type());
        Imgproc.blur(img,mean,new Size(5,5));
        Utils.matToBitmap(mean,meanBitmap);
        ivImage.setImageBitmap(meanBitmap);
    }

    public double MeanPSNR(){
        Mat GrayImg = new Mat();
        Utils.bitmapToMat(grayBitmap, GrayImg);
        Mat MeanImg = new Mat();
        Utils.bitmapToMat(meanBitmap, MeanImg);
        Mat dst = new Mat();
        Core.absdiff(GrayImg, MeanImg, dst);
        dst.convertTo(dst, CvType.CV_32F);
        Core.multiply(dst, dst, dst);
        Scalar s =  Core.sumElems(dst);
        double sse = s.val[0] + s.val[1] + s.val[2];
        if (sse <= 1e-10)
            return 0;
        else
        {
            double mse = sse / (double) (GrayImg.channels() * GrayImg.total());
            double psnr = 10 * Math.log10 ((255 * 255) / mse);
            return psnr;
        }
    }
    public  void Gaussianblur(View v){
        Mat img = new Mat();
        Utils.bitmapToMat(noiseBitmap,img);
        GaussianblurBitmap = noiseBitmap.copy(Bitmap.Config.RGB_565,true);
        Mat GaussianBlur = new  Mat(img.size(),img.type());
        Imgproc.GaussianBlur(img,GaussianBlur,new Size(5,5),2,2,1);
        Utils.matToBitmap(GaussianBlur,GaussianblurBitmap);
        ivImage.setImageBitmap(GaussianblurBitmap);
    }

   public double GausssianPSNR(){
       Mat GrayImg = new Mat();
       Utils.bitmapToMat(grayBitmap, GrayImg);
       Mat GaussianImg = new Mat();
       Utils.bitmapToMat(GaussianblurBitmap, GaussianImg);
       Mat dst = new Mat();
       Core.absdiff(GrayImg, GaussianImg, dst);
       dst.convertTo(dst, CvType.CV_32F);
       Core.multiply(dst, dst, dst);
       Scalar s =  Core.sumElems(dst);
       double sse = s.val[0] + s.val[1] + s.val[2];
       if (sse <= 1e-10)
           return 0;
       else
       {
           double mse = sse / (double) (GrayImg.channels() * GrayImg.total());
           double psnr = 10 * Math.log10 ((255 * 255) / mse);
           return psnr;
       }
   }

    public  void BilateralFilter(View v){
        Mat img = new Mat();
        Utils.bitmapToMat(noiseBitmap,img);
        Imgproc.cvtColor(img,img,Imgproc.COLOR_BGRA2BGR);
        BilateralFilterBitmap=noiseBitmap.copy(Bitmap.Config.RGB_565,true);
        Mat BilateralBlur= img.clone();
        Imgproc.bilateralFilter(img,BilateralBlur,10,250,50);
        Imgproc.cvtColor(img,img,Imgproc.COLOR_RGB2RGBA);
        Utils.matToBitmap(BilateralBlur,BilateralFilterBitmap);
        ivImage.setImageBitmap(BilateralFilterBitmap);
    }

    public double BilateralPSNR(){
        Mat GrayImg = new Mat();
        Utils.bitmapToMat(grayBitmap, GrayImg);
        Mat BilateralImg = new Mat();
        Utils.bitmapToMat(BilateralFilterBitmap, BilateralImg);
        Mat dst = new Mat();
        Core.absdiff(GrayImg, BilateralImg, dst);
        dst.convertTo(dst, CvType.CV_32F);
        Core.multiply(dst, dst, dst);
        Scalar s =  Core.sumElems(dst);
        double sse = s.val[0] + s.val[1] + s.val[2];
        if (sse <= 1e-10)
            return 0;
        else
        {
            double mse = sse / (double) (GrayImg.channels() * GrayImg.total());
            double psnr = 10 * Math.log10 ((255 * 255) / mse);
            return psnr;
        }
    }
    public void WienerFilter(View v){
        /*
        F0=fftshift(fft2(imt));     //make fft to to nosie image
        LA = fftshift(fft2(ima));   //make fft to original image
        K=0.1;                      // add constant to degradation function
        for u=1:m
        for  v=1:n
        H(u,v) = F0(u,v)/LA(u,v);  //get degradation funtion
        //  Compute wiener filter function
        H0(u,v)=(abs(H(u,v)))^2;
        H1(u,v)=H0(u,v)/(H(u,v)*(H0(u,v)+K));
        F2(u,v)=H1(u,v)*F0(u,v);
         */
        Mat GrayImg = new Mat();
        Utils.bitmapToMat(grayBitmap, GrayImg);
        Mat noiseImg = new Mat();
        Utils.bitmapToMat(noiseBitmap, noiseImg);
        Core.dft(GrayImg,GrayImg);
        Core.dft(noiseImg,noiseImg);
        Mat dst = new Mat();
        Core.divide(GrayImg,noiseImg,dst);
        Mat dst1 = new Mat();
        Core.convertScaleAbs(dst,dst1);
        Core.multiply(dst1,dst1,dst1);
        Mat dst2 =new Mat();
        Core.addWeighted(dst1,1,null,0,0.5,dst2);
        Core.multiply(dst,dst2,dst2);
        Core.divide(dst1,dst2,dst2);
        Core.multiply(dst2,GrayImg,dst2);
        Core.idft(dst2,dst2);
        Core.convertScaleAbs(dst2,dst2);
        Core.normalize(dst2,dst2,0,255,Core.NORM_MINMAX);
        dst2.convertTo(dst2,CvType.CV_8UC1);
        Utils.matToBitmap(dst2,WienerBitmap);
        ivImage.setImageBitmap(WienerBitmap);
    }
    public  void Inpaint(View v){

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

                ivImage.setImageBitmap(grayBitmap);
            }
        }

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