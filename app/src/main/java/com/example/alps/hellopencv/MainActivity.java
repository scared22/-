package com.example.alps.hellopencv;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;
    ImageView img_main;
    ImageButton img_rotation;
    public int mDegree =0;
    Bitmap mains;

    private Uri mImageCaptureUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button img_call = (Button)findViewById(R.id.img_call);
        Button img_select = (Button)findViewById(R.id.img_select);
        Button img_save = (Button)findViewById(R.id.img_save);

        img_main = (ImageView)findViewById(R.id.img_main);
        img_rotation = (ImageButton)findViewById(R.id.img_rotation);

        img_call.setOnClickListener(this);
        img_select.setOnClickListener(this);
        img_save.setOnClickListener(this);
        img_rotation.setOnClickListener(this);
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
            //이미지저장 구간
            if(mains == null)Toast.makeText(this,"이미지 없이 저장 할 수 없습니다",Toast.LENGTH_SHORT).show();
            else saveImage(mains);
        }
        if(v.getId()==R.id.img_select)
        {
            //영상처리 구간
        }
        if(v.getId()==R.id.img_rotation)
        {
            //이미지 회전구간
            if(mains == null)Toast.makeText(this,"이미지를 선택해주세요~~~!!!",Toast.LENGTH_SHORT).show();

            else
            {
                mDegree += 90;
                img_main.setImageBitmap(rotateImage(mains, mDegree));
            }
        }
    }

    public void AlbumAction()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent,PICK_FROM_ALBUM);
    }
    //이미지 저장
    public void saveImage(Bitmap bm)
    {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
        + "/lastImage.jpg");

        //File dir = file.getParentFile();
        //if(!dir.isDirectory())
        //    dir.mkdirs();
        //if(file.isDirectory())
        //    file.delete();

        //if(!file.exists())
        //    file.mkdir();



        try {
            //file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 92, out);
            Toast.makeText(this,"저장 완료~!",Toast.LENGTH_SHORT).show();
            out.close();
        } catch (Exception e) {
            file.delete();
            e.printStackTrace();}

    }

    // 이미지 회전 함수
    public Bitmap rotateImage(Bitmap src, float degree) {
        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
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
                    mains = bm;
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

                if(extras != null)
                {
                    Bitmap photo = extras.getParcelable("data");
                    mains = photo;
                    img_main.setImageBitmap(photo);
                    break;
                }

                File f = new File(mImageCaptureUri.getPath());
                if(f.exists()) f.delete();
            }
        }
    }
}
