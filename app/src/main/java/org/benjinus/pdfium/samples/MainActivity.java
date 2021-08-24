package org.benjinus.pdfium.samples;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.ImageView;

import java.io.File;

import org.benjinus.pdfium.Meta;
import org.benjinus.pdfium.PdfiumSDK;
import org.benjinus.pdfium.search.TextSearchContext;
import org.benjinus.pdfium.util.Size;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;
    PdfiumSDK mSdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.imageView);
        mSdk = new PdfiumSDK();
        int width = getScreenWidth();
        int height = getScreenHeight();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        decodePDFPage();
        findViewById(R.id.button).setOnClickListener((view) -> {
            TextSearchContext search = mSdk.newPageSearch(0, "Parameters", false, false);
            RectF location = search.searchNext();
            RectF location2 = mSdk.measureCharacterBox(0, 8);
            Log.d("PDFIUM", "Rect: " + mSdk.extractText(0, location2) );
            Log.d("PDFIUM", "Count: " + search.countResult() );
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mSdk.renderPageBitmap(bitmap, 0, 0, 0, width, height, true);
            Bitmap findTextBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int color = 0xff424242;
            Paint paint = new Paint();
            Canvas canvas = new Canvas(findTextBitmap);

            paint.setColor(color);
            canvas.drawBitmap(bitmap, new Matrix(), new Paint());
            canvas.drawRect(location, paint);
            mImageView.setImageBitmap(findTextBitmap);
        });
    }

    private void decodePDFPage() {
        try {

            File pdfFile = ((SamplesApplication) getApplication()).createNewSampleFile("Sample.pdf");

            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, MODE_READ_ONLY);


            mSdk.newDocument(fileDescriptor);

            Log.d("PDFIUM", "Page count: " + mSdk.getPageCount());

            Meta meta = mSdk.getDocumentMeta();
            Log.d("PDFIUM", meta.toString());

            mSdk.openPage(0);

            Size size = mSdk.getPageSize(0);
            Log.d("PDFIUM", "Page size: " + size.toString());

            int width = getScreenWidth();
            int height = getScreenHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mSdk.renderPageBitmap(bitmap, 0, 0, 0, width, height, true);

            mImageView.setImageBitmap(bitmap);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSdk.closeDocument();
    }
}
