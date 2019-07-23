package com.maxsavteam.newmcalc;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List< ArrayList<String> > mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context con;
    SharedPreferences sp;
    boolean DarkMode;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, ArrayList< ArrayList<String> > data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        con = context;
        sp = PreferenceManager.getDefaultSharedPreferences(con);
        DarkMode = sp.getBoolean("dark_mode", false);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /*if(DarkMode)
            con.setTheme(android.R.style.Theme_Material_NoActionBar);*/
        View view = mInflater.inflate(R.layout.recycle_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String animal = mData.get(position).get(0);
        String ans = mData.get(position).get(1);
        holder.myTextView.setText(animal);
        holder.myTextView2.setText(ans);
        if(DarkMode){
            holder.myTextView.setTextColor(con.getResources().getColor(R.color.white));
            holder.myTextView2.setTextColor(con.getResources().getColor(R.color.white));
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        TextView myTextView2;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tvAns);
            myTextView2 = itemView.findViewById(R.id.tvAns2);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public class  ViewHold extends RecyclerView.ViewHolder implements View.OnTouchListener{

        TextView myTextView;
        TextView myTextView2;

        ViewHold(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tvAns);
            myTextView2 = itemView.findViewById(R.id.tvAns2);
            itemView.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    }

    // convenience method for getting data at click position
    ArrayList <String> getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    /*void setItemTouchListener(RecyclerView.OnItemTouchListener itemTouchListener){
        this.itemTouchListener = itemTouchListener;
    }*/

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
