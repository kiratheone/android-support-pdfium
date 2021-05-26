package org.benjinus.pdfium.samples;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
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
import org.benjinus.pdfium.util.Size;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.imageView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        decodePDFPage();
    }

    private void decodePDFPage() {
        try {

            File pdfFile = ((SamplesApplication) getApplication()).createNewSampleFile("Sample.pdf");

            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, MODE_READ_ONLY);

            PdfiumSDK sdk = new PdfiumSDK();
            sdk.newDocument(fileDescriptor);

            Log.d("PDFIUM", "Page count: " + sdk.getPageCount());

            Meta meta = sdk.getDocumentMeta();
            Log.d("PDFIUM", meta.toString());

            sdk.openPage(0);

            Size size = sdk.getPageSize(0);
            Log.d("PDFIUM", "Page size: " + size.toString());

            int width = getScreenWidth();
            int height = getScreenHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            sdk.renderPageBitmap(bitmap, 0, 0, 0, width, height, true);

            mImageView.setImageBitmap(bitmap);

            sdk.closeDocument();

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

}
