package com.maxsavteam.newmcalc2.swipes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper.*;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.maxsavteam.newmcalc2.R;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

enum ButtonsState {
	GONE,
	LEFT_VISIBLE,
	RIGHT_VISIBLE
}
@SuppressLint("ClickableViewAccessibility")
public class SwipeController extends Callback {

	private boolean swipeBack = false;

	private ButtonsState buttonShowedState = ButtonsState.GONE;

	private RectF buttonInstance = null;

	private RecyclerView.ViewHolder currentItemViewHolder = null;

	private SwipeControllerActions buttonsActions;

	private static float buttonWidth = 250;

	private final Context context;

	public SwipeController(SwipeControllerActions buttonsActions, Context context) {
		this.buttonsActions = buttonsActions;
		this.context = context;
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		Point sizes = new Point();
		d.getSize(sizes);
		buttonWidth = sizes.x / 5.0f;
	}

	@Override
	public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
		return makeMovementFlags(0, LEFT | RIGHT);
	}

	@Override
	public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
		return false;
	}

	@Override
	public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

	}

	@Override
	public int convertToAbsoluteDirection(int flags, int layoutDirection) {
		if (swipeBack) {
			swipeBack = buttonShowedState != ButtonsState.GONE;
			return 0;
		}
		return super.convertToAbsoluteDirection(flags, layoutDirection);
	}

	@Override
	public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
		if (actionState == ACTION_STATE_SWIPE) {
			if (buttonShowedState != ButtonsState.GONE) {
				if (buttonShowedState == ButtonsState.LEFT_VISIBLE) dX = Math.max(dX, buttonWidth);
				if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) dX = Math.min(dX, -buttonWidth);
				super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			}
			else {
				setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			}
		}

		if (buttonShowedState == ButtonsState.GONE) {
			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
		}
		drawButtons(c, viewHolder);
		currentItemViewHolder = viewHolder;
	}

	private void setTouchListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
		recyclerView.setOnTouchListener( (v, event)->{
			swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
			if (swipeBack) {
				if (dX < -buttonWidth) buttonShowedState = ButtonsState.RIGHT_VISIBLE;
				else if (dX > buttonWidth) buttonShowedState  = ButtonsState.LEFT_VISIBLE;

				if (buttonShowedState != ButtonsState.GONE) {
					setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
					setItemsClickable(recyclerView, false);
				}
			}
			return false;
		} );
	}

	private void setTouchDownListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
		recyclerView.setOnTouchListener( (v, event)->{
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			}
			return false;
		} );
	}

	private void setTouchUpListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
		recyclerView.setOnTouchListener( (v, event)->{
			if (event.getAction() == MotionEvent.ACTION_UP) {
				SwipeController.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);
				recyclerView.setOnTouchListener( (v1, event1)->false );
				setItemsClickable(recyclerView, true);
				swipeBack = false;

				if (buttonsActions != null && buttonInstance != null && buttonInstance.contains(event.getX(), event.getY())) {
					if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
						buttonsActions.onLeftClicked(viewHolder.getAdapterPosition());
					}
					else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
						buttonsActions.onRightClicked(viewHolder.getAdapterPosition());
					}
				}
				buttonShowedState = ButtonsState.GONE;
				currentItemViewHolder = null;
			}
			return false;
		} );
	}

	private void setItemsClickable(RecyclerView recyclerView, boolean isClickable) {
		for (int i = 0; i < recyclerView.getChildCount(); ++i) {
			recyclerView.getChildAt(i).setClickable(isClickable);
		}
	}

	private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder) {
		float buttonWidthWithoutPadding = buttonWidth - 20;
		float corners = 14;

		View itemView = viewHolder.itemView;
		Paint p = new Paint();

		RectF leftButton = new RectF(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + buttonWidthWithoutPadding, itemView.getBottom());
		p.setColor(Color.BLUE);
		c.drawRoundRect(leftButton, corners, corners, p);
		drawText(context.getResources().getString(R.string.info).toUpperCase(), c, leftButton, p);

		RectF rightButton = new RectF(itemView.getRight() - buttonWidthWithoutPadding, itemView.getTop(), itemView.getRight(), itemView.getBottom());
		p.setColor(Color.RED);
		c.drawRoundRect(rightButton, corners, corners, p);
		drawText(context.getResources().getString(R.string.delete).toUpperCase(), c, rightButton, p);

		buttonInstance = null;
		if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
			buttonInstance = leftButton;
		}
		else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
			buttonInstance = rightButton;
		}
	}

	private void drawText(String text, Canvas c, RectF button, Paint p) {
		float textSize = buttonWidth / 5;
		p.setColor(Color.WHITE);
		p.setAntiAlias(true);
		p.setTextSize(textSize);

		float textWidth = p.measureText(text);
		c.drawText(text, button.centerX()-(textWidth/2), button.centerY()+(textSize/2), p);
	}

	public void onDraw(Canvas c) {
		if (currentItemViewHolder != null) {
			drawButtons(c, currentItemViewHolder);
		}
	}
}

