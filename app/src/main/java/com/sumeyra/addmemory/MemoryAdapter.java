package com.sumeyra.addmemory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sumeyra.addmemory.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.MemoryHolder> {
    ArrayList<Memories> memoriesArrayList;

    public MemoryAdapter(ArrayList<Memories> memoriesArrayList) {
        this.memoriesArrayList = memoriesArrayList;
    }


    @NonNull
    @Override
    public MemoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding= RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new MemoryHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(MemoryAdapter.MemoryHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.rowBinding.recyclerTextView.setText(memoriesArrayList.get(position).date);
        holder.itemView.setOnClickListener(v -> {
            Intent intent= new Intent(holder.itemView.getContext(),DetailsActivity.class);
            intent.putExtra("info","old");
            intent.putExtra("memoryId",memoriesArrayList.get(position).id);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return memoriesArrayList.size();
    } //2

    public static class MemoryHolder extends RecyclerView.ViewHolder{
        private final RecyclerRowBinding rowBinding;
        //Görünümü xml splitte oluşturdum bunu da Binding ile RecyclerRowBinding ile aldım dönüşü de bir binding oldu
        //sonrasında görünümü binding.getRoot() ile görünümü alabildim
        public MemoryHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.rowBinding=binding;
        }
    } //1

}
