package com.example.opencvdemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import static org.opencv.core.Core.FONT_HERSHEY_COMPLEX;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2YCrCb;
import static org.opencv.imgproc.Imgproc.FLOODFILL_FIXED_RANGE;
import static org.opencv.imgproc.Imgproc.FLOODFILL_MASK_ONLY;
import static org.opencv.imgproc.Imgproc.LINE_4;
import static org.opencv.imgproc.Imgproc.LINE_AA;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class MainActivity extends AppCompatActivity {

    Button btGet;
    Button btGrey;
    Button btGreen;
    ImageView img;
    Uri imgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewInit();
        openInit();
    }

    private void openInit() {
        System.loadLibrary("opencv_java3");
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, getApplicationContext(), new LoaderCallbackInterface() {
            @Override
            public void onManagerConnected(int status) {
                Toast.makeText(getApplicationContext(), "OpenCV connected, status: " + status, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPackageInstall(int operation, InstallCallbackInterface callback) {
                callback.install();
            }
        });

    }

    private void viewInit() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        img = findViewById(R.id.img);

        imgUri = Uri.fromFile(new File(getExternalFilesDir(Environment.DIRECTORY_DCIM),"capture.jpg"));
        btGet = findViewById(R.id.bt_get);
        btGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //调用照相机
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                startActivityForResult(intent, 0);
            }
        });
        btGrey = findViewById(R.id.bt_grey);
        btGrey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imgUri == null) return;
                Mat mat = new Mat();
                Bitmap bitmap = BitmapFactory.decodeFile(imgUri.getPath());
                Utils.bitmapToMat(bitmap, mat);
                Imgproc.cvtColor(mat, mat, COLOR_BGR2GRAY);
                Utils.matToBitmap(mat, bitmap);
                img.setImageBitmap(bitmap);
            }
        });
        btGreen = findViewById(R.id.bt_green);
        btGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imgUri == null) return;
                Mat mat = new Mat();
                Bitmap bitmap = BitmapFactory.decodeFile(imgUri.getPath());
                Utils.bitmapToMat(bitmap, mat);
                alignMat(mat);
                Utils.matToBitmap(mat, bitmap);
                img.setImageBitmap(bitmap);
            }
        });
    }

    private void alignMat(Mat src) {
        Size size = src.size();
        Mat gray = new Mat(src.size(), CV_8UC3);
        Mat yuv = new Mat(size, CV_8UC3);
        Mat faceOutline = Mat.zeros(size, CV_8UC3);
        Mat srcClone = src.clone();
        Scalar color = new Scalar(255, 255, 0);
        Imgproc.cvtColor(src, gray, COLOR_BGR2GRAY);
        Imgproc.cvtColor(src, yuv, COLOR_BGR2YCrCb); // 色彩空间转换

        //画笑脸开始
        double sw = size.width;
        double sh = size.height;
        int thickness = 4;
        double faceH = sh / 2 * 70 / 100;
        double faceW = faceH * 72 / 100;
        Imgproc.ellipse(faceOutline, new Point(sw / 2, sh / 2), new Size(faceW, faceH), 0.0, 0.0, 360.0, color, thickness, LINE_4 ,0);
        double eyeW = faceW * 23 / 100;
        double eyeH = faceH * 11 / 100;
        double eyeX = faceW * 48 / 100;
        double eyeY = faceH * 13 / 100;
        Size eyeSize = new Size(eyeW, eyeH);
        int eyeA = 15;
        int eyeYshift = 11;
        Imgproc.ellipse(faceOutline, new Point(sw / 2 - eyeX, sh / 2 - eyeY), eyeSize, 0, 180 + eyeA, 360 - eyeA, color, thickness, LINE_AA, 0);
        Imgproc.ellipse(faceOutline, new Point(sw / 2 - eyeX, sh / 2 - eyeY - eyeYshift), eyeSize, 0, 0 + eyeA, 180 - eyeA, color, thickness, LINE_AA, 0);
        Imgproc.ellipse(faceOutline, new Point(sw / 2 + eyeX, sh / 2 - eyeY), eyeSize, 0, 180 + eyeA, 360 - eyeA, color, thickness, LINE_AA, 0);
        Imgproc.ellipse(faceOutline, new Point(sw / 2 + eyeX, sh / 2 - eyeY - eyeYshift), eyeSize, 0, 0 + eyeA, 180 - eyeA, color, thickness, LINE_AA, 0);
        double mouthY = faceH * 48 / 100;
        double mouthW = faceW * 45 / 100;
        double mouthH = faceH * 6 / 100;
        Imgproc.ellipse(faceOutline, new Point(sw / 2, sh / 2 + mouthY), new Size(mouthW, mouthH), 0, 0, 180, color, thickness, LINE_AA, 0);
        int fontFace = FONT_HERSHEY_COMPLEX;
        float fontScale = 1.0f;
        int fontThickness = 2;
        String szMsg = "Put your face here";
        Imgproc.putText(faceOutline, szMsg, new Point(sw * 23 / 100, sh * 10 / 100), fontFace, fontScale, color, fontThickness, LINE_AA, false);
        //画笑脸结束

        //中值+拉普拉斯+闭运算进行轮廓提取
        Mat mask, maskPlusBorder;
        maskPlusBorder = Mat.zeros((int)sh + 2, (int)sw + 2, CV_8UC1);
        mask = maskPlusBorder.submat(new Rect(1, 1, (int)sw, (int)sh));
        Imgproc.medianBlur(gray, gray, 7);
        Mat edges = gray.clone();
        Imgproc.Laplacian(gray, edges, CV_8U);
        Imgproc.resize(edges, mask, size);
	    int EDGES_THRESHOLD = 80;
        Imgproc.threshold(mask, mask, EDGES_THRESHOLD, 255, THRESH_BINARY);
        Imgproc.dilate(mask, mask, new Mat());
        Imgproc.erode(mask, mask, new Mat());

        //种子像素点
        int NUM_SKIN_POINTS = 6;
        Point[] skinPts = new Point[NUM_SKIN_POINTS];
        skinPts[0] = new Point(sw / 2, sh / 2 - sh / 6);
        skinPts[1] = new Point(sw / 2 - sw / 11, sh / 2 - sh / 6);
        skinPts[2] = new Point(sw / 2 + sw / 11, sh / 2 - sh / 6);
        skinPts[3] = new Point(sw / 2, sh / 2 - sh / 16);
        skinPts[4] = new Point(sw / 2 - sw / 9, sh / 2 - sh / 16);
        skinPts[5] = new Point(sw / 2 + sw / 9, sh / 2 - sh / 16);

        //上下限
	    int LOWER_Y = 60;
	    int UPPER_Y = 80;
	    int LOWER_Cr = 25;
	    int UPPER_Cr = 15;
	    int LOWER_Cb = 20;
	    int UPPER_Cb = 15;

        Scalar lowerDiff = new Scalar(LOWER_Y, LOWER_Cr, LOWER_Cb);
        Scalar upperDiff = new Scalar(UPPER_Y, UPPER_Cr, UPPER_Cb);

        //漫水填充
	    int CONNECTED_COMPONENTS = 4;
	    int flags = CONNECTED_COMPONENTS | FLOODFILL_FIXED_RANGE | FLOODFILL_MASK_ONLY;
        Mat edgeMask = mask.clone();
        for (int i = 0; i < NUM_SKIN_POINTS; i++) {
            Imgproc.floodFill(yuv, maskPlusBorder, skinPts[i], new Scalar(255, 255, 255), null, lowerDiff, upperDiff, flags);
        }

        //最后叠加矩阵
        Imgproc.threshold(mask, mask, 0, 255, THRESH_BINARY);
        Core.subtract(mask, edgeMask, mask);
        int Red = 0;
        int Green = 70;
        int Blue = 0;
        Core.add(src, new Scalar(Red, Green, Blue), src, mask);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == RESULT_OK){
            img.setImageBitmap(BitmapFactory.decodeFile(imgUri.getPath()));
        }
    }
}
