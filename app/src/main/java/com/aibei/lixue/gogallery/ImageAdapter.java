package com.aibei.lixue.gogallery;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * 图片适配器
 * Created by Administrator on 2016/9/21.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private int[] imageids;

    public ImageAdapter(Context contexts,int[] images){
        this.mContext = contexts;
        this.imageids = images;
    }

    @Override
    public int getCount() {
        return imageids.length;
    }

    @Override
    public Object getItem(int i) {
        return imageids[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null){
            holder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item_images,null);
            holder.holder_imageview = (ImageView) view.findViewById(R.id.item_image);
            holder.holder_imageview.setScaleType(ImageView.ScaleType.FIT_XY);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        holder.holder_imageview.setImageResource(imageids[i]);
        holder.holder_imageview.setScaleX(0.9f);
        holder.holder_imageview.setScaleY(0.9f);
        return view;
    }

    class ViewHolder{
        ImageView holder_imageview;
    }
}
