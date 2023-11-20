package com.sumeyra.addmemory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.sumeyra.addmemory.databinding.ActivityDetailsBinding;

import java.io.ByteArrayOutputStream;

public class DetailsActivity extends AppCompatActivity {
   private ActivityDetailsBinding binding;
   ActivityResultLauncher<Intent> activityResultLauncher;
   ActivityResultLauncher<String> permissionLauncher;
   Bitmap selectedImage;
   SQLiteDatabase sqLiteDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();
        sqLiteDatabase=this.openOrCreateDatabase("Memories",MODE_PRIVATE,null);


        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        assert info != null;
        if (info.matches("new")){
            // new add
            binding.dateText.setText("");
            binding.textText.setText("");
            binding.button.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.selectimage);
        }else{
        //id e göre veri eşleme
            int memoryId = intent.getIntExtra("memoryId",1);
            binding.button.setVisibility(View.INVISIBLE);

            try {
                Cursor cursor =sqLiteDatabase.rawQuery("SELECT * FROM memories WHERE id= ?",new String[]{String.valueOf(memoryId)});
                int dateIx= cursor.getColumnIndex("dateText");
                int textIx= cursor.getColumnIndex("textText");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.textText.setText(cursor.getString(textIx));
                    binding.dateText.setText(cursor.getString(dateIx));
                    byte[] bytes= cursor.getBlob(imageIx);

                    Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }cursor.close();


            }catch(Exception e){
                e.printStackTrace();
            }
        }

        }
    public void selectImage(View view){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //Android 33 ve üstü -> READ_MEDIA_IMAGES
//izin verilmemiş
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //izin verilmedi o zmn ilk mantıklı bir açıklama sun
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES )){
                    //request permission depends on rationale reason
                    Snackbar.make(view,"Permission needed for gallery to add image",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", v -> {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                    }).show();
                }else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);

                }
            }else{
                //go to gallery with intent
                Intent intentForGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentForGallery);
            }
        }else{
            //Android 32 ve altı -> READ_EXTERNAL_STORAGES
            //izin verilmemiş
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmedi o zmn ilk mantıklı bir açıklama sun
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE )){
                    //request permission depends on rationale reason
                    Snackbar.make(view,"Permission needed for gallery to add image",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", v -> {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }).show();
                }else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                }
            }else{
                //go to gallery with intent
                Intent intentForGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentForGallery);
            }

        }

    }
    public void registerLauncher(){
        //2
        activityResultLauncher= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
            //kullanıcı gallery e gittiğinde birçok şey olabilir seçim yaptığını doğrulamamız lazım
            // result(o) dan getdata ile intent e intentten get data ile uri ye getiriyorum
            if (o.getResultCode()== RESULT_OK){
                Intent intentFromResult= o.getData();
                if (intentFromResult != null){
                    Uri imageData= intentFromResult.getData();
                    //binding.imageView.setImageURI(imageData); //burada verinin nerede olduğuna değil database için bitmap hali gerekli
                    try {
                        //SDK 28 den büyükse eğer uri yi bitmap hale getiyorum.
                        if (Build.VERSION.SDK_INT >= 28){
                            assert imageData != null;
                            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),imageData);
                            selectedImage = ImageDecoder.decodeBitmap(source);
                            binding.imageView.setImageBitmap(selectedImage);
                        } else{
                            selectedImage= MediaStore.Images.Media.getBitmap(DetailsActivity.this.getContentResolver(),imageData);
                            binding.imageView.setImageBitmap(selectedImage);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        //1
        //izin isteme eylemi
        permissionLauncher= registerForActivityResult(new ActivityResultContracts.RequestPermission(), o -> {
            if(o){
                // permission granted
                Intent intentForGallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // go to Gallery
                activityResultLauncher.launch(intentForGallery);
            }else{
                //permission denied
                Toast.makeText(DetailsActivity.this,"Permission Needed",Toast.LENGTH_LONG).show();
            }
        });
    }
    public void save(View view){
        String date= binding.dateText.getText().toString();
        String text= binding.textText.getText().toString();
        //görseli bitmap olarak direkt kullanabilirz ama biz küçültmek istiyoruz
        //bunu içn de method yazmamız gerek
        Bitmap smalledImage= makeSmallerImage(selectedImage,300);

        //SQLite içine kaydedebilmek için görseli 1 0 1 1 gibi bir arraye çevirmemiz gerekiyor.
        //Bunun için de ByteArrayOutputStream diye hazır bir method var

        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        smalledImage.compress(Bitmap.CompressFormat.PNG,50,byteArrayOutputStream);
        byte[] byteArray= byteArrayOutputStream.toByteArray();

        try {
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS memories ( id INTEGER PRIMARY KEY, dateText VARCHAR,textText VARCHAR, image BLOB)");
            String sqlString= "INSERT INTO memories (id, dateText, textText, image) VALUES (?,?,?,?)";
            //verilen stringi database içinde çalıştıracak kod bu şekilde binding bağlamalarımı da yapabiliyorum;
            SQLiteStatement sqLiteStatement = sqLiteDatabase.compileStatement(sqlString);
            sqLiteStatement.bindString(1,date);
            sqLiteStatement.bindString(2,text);
            sqLiteStatement.bindBlob(3,byteArray);
            sqLiteStatement.execute();


        }catch(Exception e){
            e.printStackTrace();
        }
        Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
        //şuan içinde bulunduğum ve önceki tüm aktiviteleri kapatacağımız bir kod yazacağız
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){
        //görsel küçülten method
        // burada mantıklı bir algortima kurmam lazım çünkü görsel hem dikey hem de yatay modda olabilir.
        int width = image.getWidth();
        int height= image.getHeight();
        float bitmapRatio = (float) width/(float)height;
        if (bitmapRatio > 1){
            //yatay görsel // landscape image
            width =maximumSize;
            height= (int)(width/bitmapRatio);

        }else{
            //dikey görsel //portrait image
            height=maximumSize;
            width = (int)(height*bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,width,height,true);


    }



}
