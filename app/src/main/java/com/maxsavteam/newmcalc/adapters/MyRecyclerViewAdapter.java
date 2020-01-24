package com.maxsavteam.newmcalc.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.newmcalc.R;
import com.maxsavteam.newmcalc.types.HistoryEntry;
import com.maxsavteam.newmcalc.utils.Utils;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>{

    private ArrayList<HistoryEntry> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context con;
    private boolean DarkMode;
    private ArrayList<ViewHolder> views = new ArrayList<>();

    // data is passed into the constructor
    public MyRecyclerViewAdapter(Context context, ArrayList< HistoryEntry > data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        con = context;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(con);
        DarkMode = sp.getBoolean("dark_mode", false);
    }

    public ArrayList<ViewHolder> getViews(){
        return views;
    }

    // Array need to clear, because when you call notifyDataSetChange(), old elements don't cleared
    public void clearViews(){
        views = new ArrayList<>();
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycle_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //viewBinderHelper.bind(holexamder.swipeReve);
        HistoryEntry entry = mData.get( position );
        String ex = entry.getExample();
        String ans = entry.getAnswer();
        String desc = "";
        views.add( holder );
        holder.tvWithDesc.setText( "false" );
        holder.with_description = false;
        holder.txtDesc.setVisibility( View.GONE );
        holder.without_desc.setVisibility( View.GONE );
        holder.with_desc.setVisibility( View.GONE );
        if ( entry.getDescription() != null ) {
            holder.with_description = true;
            holder.tvWithDesc.setText( "true" );

            desc = entry.getDescription();

            int oldLength = desc.length();
            desc = Utils.trim( desc );
            if ( desc.length() == 0 ) {
                holder.tvWithDesc.setText( "false" );
                holder.with_description = false;
                mClickListener.onTrimmedDescription( "", position );
            } else {
                if ( oldLength != desc.length() ) {
                    mClickListener.onTrimmedDescription( desc, position );
                }
                holder.txtDesc.setText(desc);
                holder.txtDesc.setVisibility(View.VISIBLE);
            }
        }
        holder.desc = desc;
        holder.example.setText(ex);
        holder.answer.setText(ans);
        holder.tvPos.setText(Integer.toString(position));
        holder.txtDesc.setTextColor(con.getResources().getColor(R.color.gray_history));
        if(DarkMode){
            TextView[] textViews = {holder.example, holder.answer};
            for(TextView t : textViews){
                t.setTextColor(con.getResources().getColor(R.color.white));
            }
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{//}, View.OnTouchListener {
        TextView example;
        TextView answer;
        TextView tvPos;
        TextView tvWithDesc;
        TextView txtDesc;
        String desc = "";
        LinearLayout with_desc, without_desc;
        Button add, delete, edit;
        boolean with_description = false;

        ViewHolder(View itemView) {
            super(itemView);
            //itemView.setBackgroundTintList(parnet_layout.getBackgroundTintList());
            example = itemView.findViewById(R.id.tvAns);
            answer = itemView.findViewById(R.id.tvAns2);
            txtDesc = itemView.findViewById(R.id.txtDesc2);
            txtDesc.setTextIsSelectable(true);
            tvWithDesc = itemView.findViewById(R.id.tvWithDesc);
            tvPos = itemView.findViewById(R.id.tvPosition);
            with_desc = itemView.findViewById(R.id.with_desc);
            without_desc = itemView.findViewById(R.id.without_desc);
            add = itemView.findViewById(R.id.btn_add);
            add.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
		            mClickListener.onEditAdd((View) view.getParent().getParent(), getAdapterPosition(), "add");
	            }
            });
            delete = itemView.findViewById(R.id.btn_desc_del);
	        delete.setOnClickListener(new View.OnClickListener() {
		        @Override
		        public void onClick(View view) {
			        if(mClickListener != null){
				        mClickListener.onDescriptionDelete((View) view.getParent().getParent(), getAdapterPosition());
			        }
		        }
	        });
            edit = itemView.findViewById(R.id.btn_desc_edit);
            edit.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
		            mClickListener.onEditAdd((View) view.getParent().getParent(), getAdapterPosition(), "edit");
	            }
            });
            Button[] buttons = {add, edit};
            for(Button b : buttons){
                b.setTextColor(con.getResources().getColor(R.color.colorAccent));
            }
            itemView.setOnClickListener(this);
            if(DarkMode)
                itemView.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            else
                itemView.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        }


        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public HistoryEntry getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public interface ItemClickListener {
        void onItemClick(View view, int position);
        void onDelete(int position, ViewHolder v);
        void showInfoButtonPressed(ViewHolder view, int position);
        void onDescriptionDelete(View view, int position);
        void onEditAdd(View view, int position, String mode);
        void onTrimmedDescription(String newDescription, int position);
    }
}
