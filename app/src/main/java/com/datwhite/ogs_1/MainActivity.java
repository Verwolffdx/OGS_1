package com.datwhite.ogs_1;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final int RESULT_LOAD_IMG = 1;

    private ImageView imageView;

    private Uri imageUri;

    private ArrayDeque<Bitmap> stack = new ArrayDeque<>();

    private Button cancel;
    private Button saveBtnJpg;
    private Button saveBtnPng;

    private Button blurButton;
    private Button contrastButton;
    private Button negativeButton;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("Check", "OpenCv configured successfully");
        } else {
            Log.d("Check", "OpenCv doesn’t configured successfully");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.my_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);

            }
        });

        cancel = (Button) findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stack.removeLast();
                if (stack.isEmpty()) {
                    imageView.setImageResource(0);
                } else {
                    imageView.setImageBitmap(stack.getLast());
                }

            }
        });

        saveBtnJpg = (Button) findViewById(R.id.save_jpg);
        saveBtnJpg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isStoragePermissionGranted();

                String root = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES).toString();
                File myDir = new File(root + "/OGS 1");
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = "Image-" + n + ".jpg";
                File file = new File(myDir, fname);
                if (file.exists()) file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    ((BitmapDrawable)imageView.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    Toast.makeText(getApplicationContext(), "Сохранено в формате .jpg", Toast.LENGTH_SHORT).show();
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(file.getPath());
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    getApplicationContext().sendBroadcast(mediaScanIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        saveBtnPng = (Button) findViewById(R.id.save_png);
        saveBtnPng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isStoragePermissionGranted();

                String root = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES).toString();
                File myDir = new File(root + "/OGS 2");
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = "Image-" + n + ".png";
                File file = new File(myDir, fname);
                if (file.exists()) file.delete();
                try {
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    ((BitmapDrawable)imageView.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    Toast.makeText(getApplicationContext(), "Сохранено в формате .png", Toast.LENGTH_SHORT).show();
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(file.getPath());
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    getApplicationContext().sendBroadcast(mediaScanIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        blurButton = (Button) findViewById(R.id.blur_button);
        contrastButton = (Button) findViewById(R.id.contrast_button);
        negativeButton = (Button) findViewById(R.id.negative_button);

        blurButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setBlur();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        contrastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setContrast();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setNegative();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();

                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
                stack.addLast(selectedImage);
                imageView.setPadding(0,0,0,0);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    private void setBlur() throws FileNotFoundException {
//        InputStream imageStream = getContentResolver().openInputStream(imageUri);
//        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        Bitmap selectedImage = stack.getLast();

        Mat src = new Mat(selectedImage.getWidth(), selectedImage.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(selectedImage, src);

        Mat dst = new Mat(src.rows(), src.cols(), src.type());

        Imgproc.GaussianBlur(src, dst, new Size(99, 99), 99, 99);

        Bitmap bmp = null;
        bmp = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, bmp);

        imageView.setImageBitmap(bmp);
        stack.addLast(bmp);
    }

    private void setContrast() throws FileNotFoundException {
//        InputStream imageStream = getContentResolver().openInputStream(imageUri);
//        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        Bitmap selectedImage = stack.getLast();

        Mat src = new Mat(selectedImage.getWidth(), selectedImage.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(selectedImage, src);

        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        src.convertTo(dst, -1, 2, -100);

        Bitmap bmp = null;
        bmp = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, bmp);

        imageView.setImageBitmap(bmp);
        stack.addLast(bmp);
    }

    private void setNegative() throws FileNotFoundException {
//        InputStream imageStream = getContentResolver().openInputStream(imageUri);
//        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        Bitmap selectedImage = stack.getLast();

        Mat src = new Mat(selectedImage.getWidth(), selectedImage.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(selectedImage, src);

        Mat dst = new Mat(src.rows(), src.cols(), src.type(), new Scalar(255, 255, 255));
        Core.subtract(dst, src, dst);

        Bitmap bmp = null;
        bmp = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, bmp);

        imageView.setImageBitmap(bmp);
        stack.addLast(bmp);
    }
}