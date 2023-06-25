package com.maxsavteam.newmcalc2.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.entity.HistoryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

	private List<HistoryEntryWrapper> mData;
	private final AdapterCallback mAdapterCallback;
	private final Context mContext;
	private final ExampleCalculator exampleCalculator;
	public static final String TAG = Main2Activity.TAG + " HistoryAdapter";

	public HistoryAdapter(Context context, ArrayList<HistoryEntry> data, AdapterCallback callback, ExampleCalculator exampleCalculator) {
		mAdapterCallback = callback;
		this.mData = data.stream()
				.map(HistoryEntryWrapper::new)
				.collect(Collectors.toList());
		this.exampleCalculator = exampleCalculator;
		mContext = context;
	}

	public void updateDescription(String newDesc, int position) {
		mData.get( position ).getHistoryEntry().setDescription( newDesc );
		notifyItemChanged( position );
	}

	public void removeItem(int position){
		mData.remove(position);
		notifyItemRemoved(position);
	}

	public List<HistoryEntry> getHistoryEntries(){
		return mData.stream()
				.map(HistoryEntryWrapper::getHistoryEntry)
				.collect(Collectors.toList());
	}

	public void setHistoryEntries(List<HistoryEntry> historyEntries){
		mData = historyEntries.stream()
				.map(HistoryEntryWrapper::new)
				.collect(Collectors.toList());
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_history, parent, false );
		return new ViewHolder( view );
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		TypedValue typedValue = new TypedValue();
		mContext.getTheme().resolveAttribute( android.R.attr.windowBackground, typedValue, true );
		holder.cardView.setCardBackgroundColor( typedValue.data );
		holder.cardView.setEnabled( true );

		mContext.getTheme().resolveAttribute( R.attr.textColor, typedValue, true );
		holder.textViewExample.setTextColor( typedValue.data );
		holder.textViewAnswer.setTextColor( typedValue.data );

		HistoryEntry entry = mData.get( position ).getHistoryEntry();
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
				holder.textViewDescription.setText( desc );
				holder.textViewDescription.setVisibility( View.VISIBLE );
			}
		}else{
			holder.textViewDescription.setVisibility( View.GONE );
		}
		holder.textViewExample.setText( ex );
		holder.textViewAnswer.setText( ans );

		int id = mData.get(position).getId();

		// getEntryPositionById should be used because the variable position may reflect an incorrect position (for example, an element was deleted before the current one)
		holder.itemView.setOnClickListener( view->mAdapterCallback.onItemClick( view, getEntryPositionById(id) ) );

		holder.editDescriptionButton.setOnClickListener(v -> mAdapterCallback.onEditDescriptionButtonClick(getEntryPositionById(id)));
		holder.deleteButton.setOnClickListener(v -> mAdapterCallback.onDeleteButtonClick(getEntryPositionById(id)));
	}

	private int getEntryPositionById(int id){
		for(int i = 0; i < mData.size(); i++)
			if(mData.get(i).getId() == id)
				return i;
		return -1;
	}

	@Override
	public int getItemCount() {
		return mData.size();
	}

	public interface ExampleCalculator {
		String calculate(String example);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public final TextView textViewExample;
		public final TextView textViewAnswer;
		public final TextView textViewDescription;
		public final CardView cardView;
		public final ImageButton editDescriptionButton;
		public final ImageButton deleteButton;

		ViewHolder(View itemView) {
			super( itemView );
			textViewExample = itemView.findViewById( R.id.textview_history_example);
			textViewAnswer = itemView.findViewById( R.id.textview_history_answer);
			textViewDescription = itemView.findViewById( R.id.textview_history_description);
			textViewDescription.setTextIsSelectable( true );

			editDescriptionButton = itemView.findViewById(R.id.image_button_history_action_edit_description);
			deleteButton = itemView.findViewById(R.id.image_button_history_action_delete);

			cardView = (CardView) itemView;
		}

	}

	public interface AdapterCallback {
		void onItemClick(View view, int position);

		void onEditDescriptionButtonClick(int position);

		void onDeleteButtonClick(int position);
	}

	private static class HistoryEntryWrapper {
		private static final Random RANDOM = new Random();

		private final int id = RANDOM.nextInt();
		private final HistoryEntry historyEntry;

		public HistoryEntryWrapper(HistoryEntry historyEntry) {
			this.historyEntry = historyEntry;
		}

		public int getId() {
			return id;
		}

		public HistoryEntry getHistoryEntry() {
			return historyEntry;
		}
	}

}
