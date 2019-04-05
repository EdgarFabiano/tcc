package br.unb.cic.opencv.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;

import br.unb.cic.opencv.R;
import br.unb.cic.opencv.builder.MatBuilder;
import br.unb.cic.opencv.paint.PaintActivity;

import static br.unb.cic.opencv.util.Constants.GALLERY_PICK_REQUEST_CODE;
import static br.unb.cic.opencv.util.ImageProcessing.checkOpenCV;

public class PhotoPickActivity extends AppCompatActivity {

    PhotoView imageView;

    Bitmap bitmap, original, contour;

    static {
        checkOpenCV();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_pick);

        imageView = findViewById(R.id.imageView);
    }

    public void openGallery(View v) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_PICK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            imageView.setImageURI(imageUri);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                original = bitmap.copy(bitmap.getConfig(), false);
            } catch (IOException e) {
                Log.e(PhotoPickActivity.class.getSimpleName(), e.getMessage());
                e.printStackTrace();
            }
            imageView.setImageBitmap(bitmap);
        }
    }

    public void apply(View v) {
        float scale = imageView.getScale();
        if(contour == null && original != null) {
            Mat src = new Mat();

            Utils.bitmapToMat(original, src);

            MatBuilder dst = new MatBuilder(src)
                    .resizeIfNecessary()
                    .gaussian3()
                    .rgbToGray()
                    .bestApproach();

            Bitmap dstBitmap = Bitmap.createBitmap(dst.getMat().width(), dst.getMat().height(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(dst.getMat(), dstBitmap);

            contour = dstBitmap;
        }

        imageView.setImageBitmap(contour);
        imageView.setScale(scale);
    }

    public void inpaint(View v) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            if (bitmap != null) {
                Intent intent = new Intent();

                ContentResolver cr = getContentResolver();
                String title = "myBitmap";
                String description = "My bitmap created by Android-er";
                String savedURL = MediaStore.Images.Media.insertImage(cr, contour != null ? contour : bitmap, title, description);
                intent.putExtra("imageURL", savedURL);
                intent.setClass(this, PaintActivity.class);
                startActivity(intent);
            }
        }

    }

}