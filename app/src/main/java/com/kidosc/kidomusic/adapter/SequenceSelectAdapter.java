package com.kidosc.kidomusic.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kidosc.kidomusic.R;


/**
 * Created by jason.xu on 2018/4/28.
 */
public class SequenceSelectAdapter extends RecyclerView.Adapter<SequenceSelectAdapter.SequenceHolder> {
    private Context mContext;

    public SequenceSelectAdapter(Context context) {
        this.mContext = context;
    }

    @NonNull
    @Override
    public SequenceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sequence, parent, false);
        SequenceHolder sequenceHolder = new SequenceHolder(itemView);
        return sequenceHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SequenceHolder holder, int position) {
        //先调试这么做，到了UI弄完之后，在考虑有没有点击
        Drawable drawable = mContext.getResources().obtainTypedArray(R.array.sequence_img).getDrawable(position);
        String name = mContext.getResources().getStringArray(R.array.sequence_name)[position];
        holder.imageView.setImageDrawable(drawable);
        holder.textView.setText(name);
    }


    @Override
    public int getItemCount() {
        return 4;
    }

    class SequenceHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public SequenceHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.sequence_img);
            textView = itemView.findViewById(R.id.sequence_name);

        }
    }

}
