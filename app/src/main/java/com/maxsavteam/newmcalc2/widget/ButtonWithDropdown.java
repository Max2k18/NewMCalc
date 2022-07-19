package com.maxsavteam.newmcalc2.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.PopupMenu;

import com.maxsavteam.newmcalc2.R;

public class ButtonWithDropdown extends AppCompatButton {
	private Context mContext;
	private Object[] mElements = new Object[]{};
	private Object[] mDropdownElements = new Object[]{};
	private OnItemSelectedListener mOnItemSelectedListener;
	private int selectedItem = 0;

	public ButtonWithDropdown(Context context) {
		super( context );
		init( context );
	}

	public ButtonWithDropdown(Context context, AttributeSet attrs) {
		super( context, attrs );
		init( context );
	}

	public ButtonWithDropdown(Context context, AttributeSet attrs, int defStyleAttr) {
		super( context, attrs, defStyleAttr );
		init( context );
	}

	private void init(Context context){
		mContext = context;
		setSingleLine();
		setEllipsize( TextUtils.TruncateAt.END );
		setTextSize( TypedValue.COMPLEX_UNIT_SP, 16 );
		setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_baseline_expand_more_24, 0 );
	}

	public interface OnItemSelectedListener{
		void onItemSelected(int index);
	}

	public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
		mOnItemSelectedListener = onItemSelectedListener;
	}

	public void setElements(Object[] elements){
		setElements( elements, elements );
	}

	public void setElements(Object[] elements, Object[] dropdownElements){
		mElements = elements;
		mDropdownElements = dropdownElements;
		super.setOnClickListener( mOnClickListener );
	}

	public void setSelection(int i){
		selectedItem = i;
		setText( String.valueOf( mElements[i] ) );
	}

	public int getSelectedItemIndex() {
		return selectedItem;
	}

	@Override
	public void setMaxWidth(int maxPixels) {
		super.setMaxWidth( maxPixels );
		invalidate();
	}

	private final View.OnClickListener mOnClickListener = new View.OnClickListener(){
		@Override
		public void onClick(View view) {
			PopupMenu popupMenu = new PopupMenu( mContext, ButtonWithDropdown.this );
			Menu menu = popupMenu.getMenu();
			for(int i = 0; i < mDropdownElements.length; i++){
				menu.add( Menu.NONE, i + 1, Menu.NONE, String.valueOf( mDropdownElements[i] ) );
			}
			popupMenu.setOnMenuItemClickListener( item->{
				selectedItem = item.getItemId() - 1;
				if(mOnItemSelectedListener != null){
					mOnItemSelectedListener.onItemSelected( item.getItemId() - 1 );
				}
				setText( String.valueOf( mElements[selectedItem] ) );
				return true;
			} );
			popupMenu.show();
		}
	};

	@Override
	public void setOnClickListener(@Nullable OnClickListener l) {
		throw new UnsupportedOperationException();
	}
}

