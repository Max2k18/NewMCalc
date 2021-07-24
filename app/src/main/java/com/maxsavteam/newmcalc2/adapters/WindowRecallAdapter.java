package com.maxsavteam.newmcalc2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.newmcalc2.R;

import java.math.BigDecimal;

public class WindowRecallAdapter extends RecyclerView.Adapter<WindowRecallAdapter.ViewHolder> {
	private final BigDecimal[] mData;
	private final WindowRecallAdapterCallback mCallback;

	public WindowRecallAdapter(BigDecimal[] bd_arr, WindowRecallAdapterCallback callback){
		this.mData = bd_arr;
		mCallback = callback;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from( parent.getContext() ).inflate(R.layout.recall_recycle_raw, parent, false);
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
		holder.itemView.setOnClickListener( view->mCallback.onRecallClick( holder.itemView, position ) );
	}

	@Override
	public int getItemCount() {
		return this.mData.length;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView mNumber;
		private final TextView mResult;

		private ViewHolder(View itemView) {
			super(itemView);
			mNumber = itemView.findViewById(R.id.recall_text_number);
			mResult = itemView.findViewById(R.id.recall_text_result);
		}
	}

	public interface WindowRecallAdapterCallback {
		void onRecallClick(View view, int position);
	}
}
