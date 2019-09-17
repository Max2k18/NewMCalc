package com.maxsavteam.newmcalc.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.newmcalc.R;

import java.math.BigDecimal;

public class window_recall_adapter extends RecyclerView.Adapter<window_recall_adapter.ViewHolder> {
	private Context con;
	private LayoutInflater inflater;
	private BigDecimal[] data;
	private inter I;
	private SharedPreferences sp;
	private boolean DarkMode = false;

	public void setInterface(inter T){
		this.I = T;
	}

	public window_recall_adapter(Context c, BigDecimal[] bd_arr){
		this.con = c;
		this.data = bd_arr;
		this.inflater = LayoutInflater.from(c);
		sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
		DarkMode = sp.getBoolean("dark_mode", false);
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.recall_recycle_raw, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		String pos;
		if(position == 0)
			pos = "M";
		else
			pos = Integer.toString(position);
		String result = data[position].toString();
		holder.mResult.setText(result);
		holder.mNumder.setText(pos);
		if(DarkMode)
			holder.mResult.setTextColor(con.getResources().getColor(R.color.white));
	}

	@Override
	public int getItemCount() {
		return this.data.length;
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		TextView mNumder;
		TextView mResult;

		ViewHolder(View itemView) {
			super(itemView);
			mNumder = itemView.findViewById(R.id.recall_text_number);
			mResult = itemView.findViewById(R.id.recall_text_result);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			I.onRecallClick(view, getAdapterPosition());
		}
	}

	public interface inter{
		void onRecallClick(View view, int position);
	}
}
