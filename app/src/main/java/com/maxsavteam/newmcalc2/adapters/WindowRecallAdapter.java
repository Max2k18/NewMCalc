package com.maxsavteam.newmcalc2.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.newmcalc2.R;

import java.math.BigDecimal;

public class WindowRecallAdapter extends RecyclerView.Adapter<WindowRecallAdapter.ViewHolder> {
	private final Context mContext;
	private final LayoutInflater mLayoutInflater;
	private final BigDecimal[] mData;
	private WindowRecallAdapterCallback mCallback;
	private final boolean DarkMode;

	public void setInterface(WindowRecallAdapterCallback T){
		this.mCallback = T;
	}

	public WindowRecallAdapter(Context c, BigDecimal[] bd_arr){
		this.mContext = c;
		this.mData = bd_arr;
		this.mLayoutInflater = LayoutInflater.from(c);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( c.getApplicationContext() );
		DarkMode = sp.getBoolean("dark_mode", false);
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = mLayoutInflater.inflate(R.layout.recall_recycle_raw, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		String pos;
		if(position == 0)
			pos = "M";
		else
			pos = Integer.toString(position);
		String result = mData[position].toString();
		holder.mResult.setText(result);
		holder.mNumber.setText(pos);
		if(DarkMode)
			holder.mResult.setTextColor( mContext.getResources().getColor(R.color.white));
	}

	@Override
	public int getItemCount() {
		return this.mData.length;
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		final TextView mNumber;
		final TextView mResult;

		ViewHolder(View itemView) {
			super(itemView);
			mNumber = itemView.findViewById(R.id.recall_text_number);
			mResult = itemView.findViewById(R.id.recall_text_result);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			mCallback.onRecallClick(view, getAdapterPosition());
		}
	}

	public interface WindowRecallAdapterCallback {
		void onRecallClick(View view, int position);
	}
}
