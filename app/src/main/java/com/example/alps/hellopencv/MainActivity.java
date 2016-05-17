package com.example.alps.hellopencv;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.app.Activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;
    private String absoultePath;
    ImageView img_main;

    private Uri mImageCaptureUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button img_call = (Button)findViewById(R.id.img_call);
        Button img_select = (Button)findViewById(R.id.img_select);
        Button img_save = (Button)findViewById(R.id.img_save);
        img_main = (ImageView)findViewById(R.id.img_main);

        img_call.setOnClickListener(this);
        img_select.setOnClickListener(this);
        img_save.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.img_call)
        {
            DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PhotoAction();
                }
            };

            DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   AlbumAction();
                }
            };

            DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };
            new AlertDialog.Builder(this)
                    .setTitle("업로드할 이미지 선택")
                    .setPositiveButton("사진촬영",cameraListener)
                    .setNeutralButton("앨범선택",albumListener)
                    .setNegativeButton("취소",cancelListener)
                    .show();
        }
        if(v.getId()==R.id.img_save)
        {

        }
        if(v.getId()==R.id.img_select)
        {

        }

    }

    public void AlbumAction()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent,PICK_FROM_ALBUM);
    }

    private void storeCorpImage(Bitmap bitmap, String filePath)
    {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/SmartWheel";
        File directory_SmartWheel = new File(dirPath);

        if(!directory_SmartWheel.exists())
            directory_SmartWheel.mkdir();

        File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try{
            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));

            out.flush();
            out.close();
        }catch(Exception e){e.printStackTrace();}
    }

    public void PhotoAction()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String uri = "tmp_" + String.valueOf(System.currentTimeMillis())+".jpg";
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),uri));

        intent.putExtra(MediaStore.EXTRA_OUTPUT,mImageCaptureUri);
        startActivityForResult(intent,PICK_FROM_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode != RESULT_OK) return;
        switch(requestCode)
        {
            case PICK_FROM_ALBUM:
            {
                mImageCaptureUri = data.getData();
                try {
                    Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageCaptureUri);
                    img_main.setImageBitmap(bm);
                } catch (IOException e) {e.printStackTrace();}
                break;
            }
            case PICK_FROM_CAMERA:
            {
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri,"image/*");

                //Crop할 이미지 200*200 크기로 저장
                intent.putExtra("outputX",200);
                intent.putExtra("outputY",200);
                intent.putExtra("aspectX",1);
                intent.putExtra("aspectY",1);
                intent.putExtra("scale",true);
                intent.putExtra("return-data",true);
                startActivityForResult(intent,CROP_FROM_IMAGE);
                break;
            }
            case CROP_FROM_IMAGE:
            {
                if(resultCode!=RESULT_OK) return ;

                final Bundle extras = data.getExtras();

                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/SmartWheel"+System.currentTimeMillis()+".jpg";

                if(extras != null)
                {
                    Bitmap photo = extras.getParcelable("data");
                    img_main.setImageBitmap(photo);

                    storeCorpImage(photo,filePath);
                    absoultePath = filePath;
                    break;
                }

                File f = new File(mImageCaptureUri.getPath());
                if(f.exists()) f.delete();
            }
        }
    }
}
