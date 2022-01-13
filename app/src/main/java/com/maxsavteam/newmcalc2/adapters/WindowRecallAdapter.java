package com.maxsavteam.newmcalc2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.calculator.results.List;
import com.maxsavteam.newmcalc2.App;
import com.maxsavteam.newmcalc2.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class WindowRecallAdapter extends RecyclerView.Adapter<WindowRecallAdapter.ViewHolder> {
	private final ArrayList<List> data;
	private final WindowRecallAdapterCallback mCallback;
	private final DecimalFormat decimalFormat;

	public WindowRecallAdapter(ArrayList<List> results, WindowRecallAdapterCallback callback){
		this.data = results;
		mCallback = callback;

		decimalFormat = new DecimalFormat( "#,##0.###", new DecimalFormatSymbols( App.getInstance().getAppLocale() ) );
		decimalFormat.setParseBigDecimal( true );
		decimalFormat.setMaximumFractionDigits( 8 );
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
		String result = data.get( position ).format(decimalFormat);
		holder.result.setText(result);
		holder.number.setText(pos);
		holder.itemView.setOnClickListener( view->mCallback.onRecallClick( holder.itemView, position ) );
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView number;
		private final TextView result;

		private ViewHolder(View itemView) {
			super(itemView);
			number = itemView.findViewById(R.id.recall_text_number);
			result = itemView.findViewById(R.id.recall_text_result);
		}
	}

	public interface WindowRecallAdapterCallback {
		void onRecallClick(View view, int position);
	}
}
