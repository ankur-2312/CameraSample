package com.camerasample;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;


public class ImageDataAdapter extends RecyclerView.Adapter<ImageDataAdapter.MyViewHolder> {
    private ArrayList<Bitmap> imageBitmap;

    ImageDataAdapter(ArrayList<Bitmap> imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflateView = layoutInflater.inflate(R.layout.inflate, parent, false);
        return new MyViewHolder(inflateView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.ivImage.setImageBitmap(imageBitmap.get(position));
    }

    @Override
    public int getItemCount() {
        return imageBitmap.size();
    }

    //View holder to hold the views of inflated layout
    static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivImage;

        MyViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
    }
}