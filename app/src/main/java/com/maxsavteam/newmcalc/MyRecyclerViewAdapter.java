package com.maxsavteam.newmcalc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>{

    private ArrayList< ArrayList<String> > mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context con;
    SharedPreferences sp;
    boolean DarkMode;
    private history history;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, ArrayList< ArrayList<String> > data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        con = context;
        sp = PreferenceManager.getDefaultSharedPreferences(con);
        history = new history();
        DarkMode = sp.getBoolean("dark_mode", false);
        parnet_layout = mInflater.inflate(R.layout.activity_history, null);
    }

    View parnet_layout;

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /*if(DarkMode)
            con.setTheme(android.R.style.Theme_Material_NoActionBar);*/
        View view = mInflater.inflate(R.layout.recycle_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //viewBinderHelper.bind(holexamder.swipeReve);
        String ex = mData.get(position).get(0);
        String ans = mData.get(position).get(1);
        String desc = "";
        holder.tvWithDesc.setText("false");
        holder.with_description = false;
        holder.txtDesc.setVisibility(View.GONE);
        holder.btnInfo.setVisibility(View.GONE);
        holder.btnDel.setVisibility(View.GONE);
        holder.without_desc.setVisibility(View.GONE);
        holder.with_desc.setVisibility(View.GONE);
        if(ex.contains("~")) {
            holder.with_description = true;
            holder.tvWithDesc.setText("true");
            int i;
            for(i = 0; i < ex.length() && ex.charAt(i) != '~'; i++);
            for(int j = i + 1; j < ex.length(); j++){
                desc += ex.charAt(j);
            }
            ex = ex.substring(0, i);

            /*for(int i = 0; i < ex.length(); i++){
                if(ex.charAt(i) == '~'){
                    for(int j = i + 1; j < ex.length(); j++){
                        desc += ex.charAt(j);
                    }
                    ex = ex.substring(0, i);
                    break;
                }
            }*/
            holder.txtDesc.setText(desc);
            holder.txtDesc.setVisibility(View.VISIBLE);
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

    public ArrayList< ArrayList<String> > getArr(){
        return mData;
    }

    boolean show_checkbox_delete_all = false;
    void set_checkboxes(boolean b){
        show_checkbox_delete_all = b;
    }

    private void swipe_btn(View v, String swipe){
        ImageButton imgInfo = v.findViewById(R.id.btnInfoInRow), imgDel = v.findViewById(R.id.btnDelInRow);
        int visInfo = imgInfo.getVisibility(), visDel = imgDel.getVisibility();
        int on = View.VISIBLE, off = View.GONE;
        if(swipe.equals("left")){
            if(visInfo == off && visDel == off){
                imgInfo.setVisibility(on);
            }else if(visInfo == off && visDel == on){
                imgDel.setVisibility(off);
            }
        }else if(swipe.equals("right")){
            if(visInfo == off && visDel == off)
                imgDel.setVisibility(on);
            else if(visInfo == on && visDel == off)
                imgInfo.setVisibility(off);
        }
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, SwipeDetector.touch{//}, View.OnTouchListener {
        TextView example;
        TextView answer;
        TextView tvPos;
        TextView tvWithDesc;
        TextView txtDesc;
        String desc = "";
        ImageButton btnDel, btnInfo;
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
            btnDel = itemView.findViewById(R.id.btnDelInRow);
            btnInfo = itemView.findViewById(R.id.btnInfoInRow);
            with_desc = itemView.findViewById(R.id.with_desc);
            without_desc = itemView.findViewById(R.id.without_desc);
            add = itemView.findViewById(R.id.btn_add);
            add.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
		            mClickListener.onEdit_Add((View) view.getParent().getParent(), getAdapterPosition(), "add");
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
		            mClickListener.onEdit_Add((View) view.getParent().getParent(), getAdapterPosition(), "edit");
	            }
            });
            Button[] buttons = {add, edit};
            for(Button b : buttons){
                b.setTextColor(con.getResources().getColor(R.color.colorAccent));
            }
            //c = itemView.findViewById(R.id.checkBox);
            btnDel.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
		            View par = (View) view.getParent();
		            par = (View) par.getParent();
		            TextView t = par.findViewById(R.id.tvPosition);
		            if (mClickListener != null) {
			            mClickListener.onDelete(Integer.valueOf(t.getText().toString()), getAdapterPosition(), par);
		            }
		            //Toast.makeText(con.getApplicationContext(), con.getResources().getResourceName(par.getId()) + " " + t.getText().toString(), Toast.LENGTH_SHORT).show();
	            }
            });
            //btnDel = itemView.findViewById(R.id.btnInfoInRow);
            btnInfo.setOnClickListener(view -> {
                //View par = (View) view.getParent().getParent();
                if(mClickListener != null){
                    mClickListener.ShowInfoButtonPressed(view, getAdapterPosition());
                }
            });
            itemView.setOnClickListener(this);
            SwipeDetector sd = new SwipeDetector();
            sd.setTouch(this);
            itemView.setOnTouchListener(sd);
        }

        public int getPos(){
            return getAdapterPosition();
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public void onUpSwipe(View v, float distance) {
            //Toast.makeText(con.getApplicationContext(), "up swipe", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRightSwipe(View v, float distance) {
            //Toast.makeText(con, v.getResources().getResourceName(v.getId()) + " " + ((View) v.getParent()).getResources().getResourceName( ((View) v.getParent()).getId() ), Toast.LENGTH_SHORT).show();
            swipe_btn(v, "right");
        }

        @Override
        public void onDownSwipe(View v, float distance) {
            //Toast.makeText(con.getApplicationContext(), "down swipe", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLeftSwipe(View v, float distance) {
            swipe_btn(v, "left");
            //Toast.makeText(con.getApplicationContext(), "Left swipe", Toast.LENGTH_SHORT).show();
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
        void onDelete(int position, int adapter_position, View v);
        void ShowInfoButtonPressed(View view, int position);
        void onDescriptionDelete(View view, int position);
        void onEdit_Add(View view, int position, String mode);
        //void onTouch(View view, int position, MotionEvent event);
    }
}
