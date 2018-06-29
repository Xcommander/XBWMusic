package com.kidosc.kidomusic.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kidosc.kidomusic.R;
import com.kidosc.kidomusic.activity.MusicPlayActivity;

import com.kidosc.kidomusic.model.MusicDesInfo;

import java.util.ArrayList;


/**
 * Created by jason.xu on 2018/4/25.
 */
public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicHolder> {

    /**
     * mMusicList:存储music
     */
    private ArrayList<MusicDesInfo> mMusicList=new ArrayList<>();


    private Context mContext;


    /**
     * 内部类:自定义ViewHolder
     */
    class MusicHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public MusicHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.music_icon);
            textView = itemView.findViewById(R.id.music_name);
        }
    }

    public MusicListAdapter(Context context, ArrayList<MusicDesInfo> list) {
        this.mContext = context;
        this.mMusicList.addAll(list);
    }

    /**
     * 更新数据源，防止报错
     *
     * @param list
     */
    public void updateData(ArrayList<MusicDesInfo> list) {
        Log.e("xulinchao22", "updateData: " + list.size());
        if(mMusicList!=null){
            int oldSize=mMusicList.size();
            mMusicList.clear();
            notifyItemRangeRemoved(0,oldSize);
            mMusicList.addAll(list);
            notifyItemRangeInserted(0,mMusicList.size());

        }
    }


    @NonNull
    @Override
    public MusicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);
        MusicHolder musicHolder = new MusicHolder(itemView);
        return musicHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MusicHolder holder, final int position) {
        holder.textView.setText(mMusicList.get(position).getTitle());
        holder.imageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_icon));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到播放界面
                Intent intent = new Intent(mContext, MusicPlayActivity.class);
                intent.putExtra("position", position);
                mContext.startActivity(intent);
//                    holder.itemView.setBackgroundColor(mContext.getColor(android.R.color.transparent));

            }
        });
    }


    @Override
    public int getItemCount() {
        return mMusicList.size();
    }
}
