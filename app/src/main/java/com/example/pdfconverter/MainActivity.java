package com.example.pdfconverter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {

    public String fileName;
    public Toolbar toolbar;
    public FloatingActionButton cameraBtn;
    public FloatingActionButton galleryBtn;
    public LinearLayout image;
    public int count = 0;
    public List<Bitmap> bitmaps = new ArrayList<>();
    ActivityResultLauncher<Intent> mGetPermission;
    int j = 0;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cameraBtn = findViewById(R.id.cameraButton);
        galleryBtn = findViewById(R.id.galeryButton);


        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE
                },
                1
        );

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCameraPermission();
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
    }

    private void askCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 102);
        }else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102) {
            if (grantResults.length < 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(MainActivity.this, "Camera Permission Required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera(){
        Intent camera  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            ClipData clipData = data.getClipData();

            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    try {
                        InputStream is = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        bitmaps.add(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            } else {
                Uri imageUri = data.getData();
                try {
                    InputStream is = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    bitmaps.add(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.imageLayout);
                            for (int i=j; i<bitmaps.size(); i++){
                                ImageView image = new ImageView(MainActivity.this);
                                image.setLayoutParams(new android.view.ViewGroup.LayoutParams(700, 900));
                                image.setMaxHeight(bitmaps.get(i).getHeight());
                                image.setMaxWidth(bitmaps.get(i).getWidth());
                                linearLayout.addView(image);
                                image.setImageBitmap(bitmaps.get(i));
                                j++;
                            }
                        }
                    });
                }
            }).start();
        }

        if (requestCode == 2){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            bitmaps.add(bitmap);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.imageLayout);
                            for (int i=j; i<bitmaps.size(); i++){
                                ImageView image = new ImageView(MainActivity.this);
                                image.setLayoutParams(new android.view.ViewGroup.LayoutParams(700, 900));
                                image.setMaxHeight(bitmaps.get(i).getHeight());
                                image.setMaxWidth(bitmaps.get(i).getWidth());
                                linearLayout.addView(image);
                                image.setImageBitmap(bitmaps.get(i));
                                j++;
                            }
                        }
                    });
                }
            }).start();
        }
    }

    public void makePdf() throws Exception{
        File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), fileName);

        File myFile = new File(pdfFolder + ".pdf");
        PdfWriter pdfWriter = new PdfWriter(myFile);

        int width = 0, height = 0;
        for (int i=0; i<bitmaps.size(); i++){
            if (bitmaps.get(i).getWidth()>width && bitmaps.get(i).getHeight()>height){
                width = bitmaps.get(i).getWidth();
                height = bitmaps.get(i).getHeight();
            }else if (bitmaps.get(i).getWidth()>width){
                width = bitmaps.get(i).getWidth();
            }else if (bitmaps.get(i).getHeight()>height){
                height = bitmaps.get(i).getHeight();
            }
        }

        Rectangle pageSize = new Rectangle(width, height);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument, new PageSize(pageSize));

        for (int i=0; i<bitmaps.size(); i++) {
            Bitmap bitmap = bitmaps.get(i);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bitmapData = stream.toByteArray();

            ImageData imageData = ImageDataFactory.create(bitmapData);
            Image images = new Image(imageData);

            document.add(images);
        }
        document.close();
    }

    public void dialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_box, null);
        final EditText txtInput = (EditText) mView.findViewById(R.id.txt_input);
        Button btn_cancel = (Button) mView.findViewById(R.id.btnCancel);
        Button btn_ok = (Button) mView.findViewById(R.id.btnOk);

        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCancelable(false);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileName = txtInput.getText().toString();
                try {
                    makePdf();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                alertDialog.dismiss();

                Toast toast = Toast.makeText(getApplicationContext(), "PDF Successfully Created", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.pdfButton:
                try {
                    if (Environment.isExternalStorageManager()){
                        dialog();
                    }else{
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.filesButton:
                Intent intent = new Intent(MainActivity.this, Images.class);
                startActivity(intent);
                finish();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }
        return super.onOptionsItemSelected(item);
    }
}