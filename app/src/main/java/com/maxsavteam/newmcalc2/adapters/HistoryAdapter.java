package com.maxsavteam.newmcalc2.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.core.CalculatorWrapper;
import com.maxsavteam.newmcalc2.entity.HistoryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

	private final ArrayList<HistoryEntry> mData;
	private final AdapterCallback mAdapterCallback;
	private final Context mContext;
	private final ExampleCalculator exampleCalculator;
	private final ArrayList<ViewHolder> mViewHolders = new ArrayList<>();
	public static final String TAG = Main2Activity.TAG + " HistoryAdapter";
	private int mWaitingToDelete = -1;

	public HistoryAdapter(Context context, ArrayList<HistoryEntry> data, AdapterCallback callback, ExampleCalculator exampleCalculator) {
		mAdapterCallback = callback;
		this.mData = new ArrayList<>( data );
		this.exampleCalculator = exampleCalculator;
		mContext = context;
		for (int i = 0; i < data.size(); i++)
			mViewHolders.add( null );
	}

	public void setWaitingToDelete(int pos) {
		int temp = mWaitingToDelete;
		mWaitingToDelete = pos;
		if ( pos == -1 ) {
			notifyItemChanged( temp );
		} else {
			notifyItemChanged( pos );
		}
	}

	public void updateDescription(String newDesc, int position) {
		mData.get( position ).setDescription( newDesc );
		notifyItemChanged( position );
	}

	public List<HistoryEntry> getHistoryEntries(){
		return mData;
	}

	public void setHistoryEntries(List<HistoryEntry> historyEntries){
		mData.clear();
		mData.addAll( historyEntries );
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_history, parent, false );
		return new ViewHolder( view );
	}

	public void toggleDescriptionLayoutVisibility(int position) {
		ViewHolder holder = mViewHolders.get( position );
		if ( holder.descLayout.getVisibility() == View.VISIBLE ) {
			holder.descLayout.setVisibility( View.GONE );
		} else {
			holder.descLayout.setVisibility( View.VISIBLE );
		}
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		if ( mWaitingToDelete == position ) {
			holder.example.setTextColor( mContext.getResources().getColor( R.color.white ) );
			holder.answer.setTextColor( mContext.getResources().getColor( R.color.white ) );
			holder.cardView.setCardBackgroundColor( Color.RED );
			holder.cardView.setEnabled( false );
		} else {
			TypedValue typedValue = new TypedValue();
			mContext.getTheme().resolveAttribute( android.R.attr.windowBackground, typedValue, true );
			holder.cardView.setCardBackgroundColor( typedValue.data );
			holder.cardView.setEnabled( true );

			mContext.getTheme().resolveAttribute( R.attr.textColor, typedValue, true );
			holder.example.setTextColor( typedValue.data );
			holder.answer.setTextColor( typedValue.data );
		}
		mViewHolders.set( position, holder );
		HistoryEntry entry = mData.get( position );
		String ex = entry.getExample();
		String ans;
		if(entry.getAnswer() == null) {
			ans = exampleCalculator.calculate( ex );
			entry.setAnswer( ans );
		}else {
			ans = entry.getAnswer();
		}
		if ( entry.getDescription() != null ) {
			String desc = entry.getDescription();
			if ( desc.length() != 0 ) {
				holder.txtDesc.setText( desc );
				holder.txtDesc.setVisibility( View.VISIBLE );
			}
			holder.btnAddDesc.setVisibility( View.GONE );
			holder.btnDeleteDesc.setVisibility( View.VISIBLE );
			holder.btnEditDesc.setVisibility( View.VISIBLE );
		}else{
			holder.txtDesc.setVisibility( View.GONE );
			holder.txtDesc.setText( "" );

			holder.btnAddDesc.setVisibility( View.VISIBLE );
			holder.btnDeleteDesc.setVisibility( View.GONE );
			holder.btnEditDesc.setVisibility( View.GONE );
		}
		holder.example.setText( ex );
		holder.answer.setText( ans );
		holder.tvPos.setText( String.format( Locale.ROOT, "%d", position ) );

		holder.itemView.setOnClickListener( view->mAdapterCallback.onItemClick( view, position ) );

		holder.btnDeleteDesc.setOnClickListener( view->mAdapterCallback.onDescriptionDelete( position ) );
		holder.btnEditDesc.setOnClickListener( view->mAdapterCallback.onDescriptionEdit( position ) );
		holder.btnAddDesc.setOnClickListener( view->mAdapterCallback.onDescriptionAdd( position ) );

		holder.descLayout.setVisibility( View.GONE );
	}

	@Override
	public int getItemCount() {
		return mData.size();
	}

	public interface ExampleCalculator {
		String calculate(String example);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public final TextView example;
		public final TextView answer;
		public final TextView tvPos;
		public final TextView txtDesc;
		public final LinearLayout descLayout;
		public final LinearLayout container;
		public final Button btnAddDesc;
		public final Button btnDeleteDesc;
		public final Button btnEditDesc;
		public final CardView cardView;

		ViewHolder(View itemView) {
			super( itemView );
			example = itemView.findViewById( R.id.tvAns );
			answer = itemView.findViewById( R.id.tvAns2 );
			txtDesc = itemView.findViewById( R.id.txtDesc2 );
			txtDesc.setTextIsSelectable( true );
			tvPos = itemView.findViewById( R.id.tvPosition );
			descLayout = itemView.findViewById( R.id.descriptionActions );
			btnAddDesc = itemView.findViewById( R.id.btn_add );
			btnDeleteDesc = itemView.findViewById( R.id.btn_desc_del );
			btnEditDesc = itemView.findViewById( R.id.btn_desc_edit );
			container = itemView.findViewById( R.id.history_entry_container );

			cardView = (CardView) itemView;
		}

	}

	public interface AdapterCallback {
		void onItemClick(View view, int position);

		void onDescriptionEdit(int position);

		void onDescriptionAdd(int position);

		void onDescriptionDelete(int position);
	}
}
