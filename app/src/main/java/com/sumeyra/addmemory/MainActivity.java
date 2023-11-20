package com.sumeyra.addmemory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sumeyra.addmemory.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<Memories> memoriesArrayList;
    MemoryAdapter memoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.sumeyra.addmemory.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        memoriesArrayList = new ArrayList<Memories>();
        //layoutmanager ile görünümün grid mi linear mı olacağını ayarlıyoruz
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //sonra adapter initilize ediyoruz
        memoryAdapter= new MemoryAdapter(memoriesArrayList);
        binding.recyclerView.setAdapter(memoryAdapter);


        getData();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getData(){
         //veritabanı ile çalışacağım için try and catch açacağım
        try {
            SQLiteDatabase sqLiteDatabase= this.openOrCreateDatabase("Memories",MODE_PRIVATE,null);

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM memories",null);
            //date e göre bir liste yapacağım ve onu id ile çekeceğim
            int dateIx= cursor.getColumnIndex("dateText");
            int idIx= cursor.getColumnIndex("id");

            while(cursor.moveToNext()){
                String date= cursor.getString(dateIx);
                int id= cursor.getInt(idIx);
                //bu ikisini düzenli olarak alacağım algoritmam bu şekilde o yüzden bunu ayrı bir class yapacağım
                Memories memories= new Memories(date,id);
                memoriesArrayList.add(memories);
            }
            memoryAdapter.notifyDataSetChanged();
            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.memory_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_memory){
            Intent intent = new Intent(this,DetailsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}