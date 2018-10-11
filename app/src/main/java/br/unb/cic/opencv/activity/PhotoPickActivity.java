package br.unb.cic.opencv.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;

import br.unb.cic.opencv.R;
import br.unb.cic.opencv.util.MatBuilder;

import static br.unb.cic.opencv.util.Constants.GALLERY_PICK_REQUEST_CODE;
import static br.unb.cic.opencv.util.ImageProcessing.checkOpenCV;

public class PhotoPickActivity extends AppCompatActivity {

    ImageView imageView;

    Bitmap imageBitmap;

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
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                Log.e(PhotoPickActivity.class.getSimpleName(), e.getMessage());
                e.printStackTrace();
            }
            imageView.setImageBitmap(imageBitmap);
        }
    }

    public void apply(View v) {
        Mat rgba = new Mat();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inSampleSize = 4;

        Bitmap grayBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(imageBitmap, rgba);

        MatBuilder gray = new MatBuilder(rgba)
                .gaussian3()
                .rgbToGray()
                .sobel();

        Utils.matToBitmap(gray.getMat(), grayBitmap);

        imageView.setImageBitmap(grayBitmap);
    }
}
