package com.maxsavteam.newmcalc2.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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
import com.maxsavteam.newmcalc2.types.HistoryEntry;
import com.maxsavteam.newmcalc2.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

	private final ArrayList<HistoryEntry> mData;
	private final AdapterCallback mAdapterCallback;
	private final Context con;
	private final ArrayList<ViewHolder> mViewHolders = new ArrayList<>();
	private static final String TAG = Main2Activity.TAG + " HistoryAdapter";

	// data is passed into the constructor
	public HistoryAdapter(Context context, ArrayList<HistoryEntry> data, AdapterCallback callback) {
		mAdapterCallback = callback;
		this.mData = new ArrayList<>( data );
		con = context;
		for (int i = 0; i < data.size(); i++)
			mViewHolders.add( null );
	}

	private int mWaitingToDelete = -1;

	public void setWaitingToDelete(int pos) {
		int temp = mWaitingToDelete;
		mWaitingToDelete = pos;
		if ( pos == -1 ) {
			notifyItemChanged( temp );
		} else {
			notifyItemChanged( pos );
		}
	}

	public void remove(int position) {
		mData.remove( position );
		mViewHolders.remove( position );
		notifyItemRemoved( position );
	}

	public void updateDescription(String newDesc, int position) {
		mData.get( position ).setDescription( newDesc );
		notifyItemChanged( position );
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.recycle_row, parent, false );
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

	// binds the data to the TextView in each row
	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		if ( mWaitingToDelete == position ) {
			holder.example.setTextColor( con.getResources().getColor( R.color.white ) );
			holder.answer.setTextColor( con.getResources().getColor( R.color.white ) );
			holder.cardView.setCardBackgroundColor( Color.RED );
			holder.cardView.setEnabled( false );
		} else {
			TypedValue typedValue = new TypedValue();
			con.getTheme().resolveAttribute( android.R.attr.windowBackground, typedValue, true );
			holder.cardView.setCardBackgroundColor( typedValue.data );
			holder.cardView.setEnabled( true );

			con.getTheme().resolveAttribute( R.attr.textColor, typedValue, true );
			holder.example.setTextColor( typedValue.data );
			holder.answer.setTextColor( typedValue.data );
		}
		mViewHolders.set( position, holder );
		HistoryEntry entry = mData.get( position );
		String ex = entry.getExample();
		String ans = entry.getAnswer();
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

	// total number of rows
	@Override
	public int getItemCount() {
		return mData.size();
	}

	// stores and recycles views as they are scrolled off screen
	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView example;
		TextView answer;
		TextView tvPos;
		TextView txtDesc;
		LinearLayout descLayout, container;
		Button btnAddDesc, btnDeleteDesc, btnEditDesc;
		CardView cardView;

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

		boolean onDelete(int position);

		void onDescriptionEdit(int position);

		void onDescriptionAdd(int position);

		void onDescriptionDelete(int position);
	}
}
