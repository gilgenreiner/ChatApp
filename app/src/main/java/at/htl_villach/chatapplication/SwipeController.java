package at.htl_villach.chatapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.view.MotionEvent;
import android.view.View;

import at.htl_villach.chatapplication.adapters.ChatAdapter;

import static android.support.v7.widget.helper.ItemTouchHelper.*;

enum LabelsState {
    GONE,
    RIGHT_VISIBLE,
    LEFT_VISIBLE
}

class SwipeController extends Callback {
    private static final float labelWidth = 200;
    public static final int MSG_TYPE_LEFT = 0;

    private boolean swipeBack = false;
    private LabelsState labelShowedState = LabelsState.GONE;
    private RecyclerView.ViewHolder currentItemViewHolder = null;

    public SwipeController() {

    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getItemViewType() == MSG_TYPE_LEFT)
            return makeMovementFlags(0, RIGHT);
        else
            return makeMovementFlags(0, LEFT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = labelShowedState != LabelsState.GONE;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ACTION_STATE_SWIPE) {
            if (labelShowedState != LabelsState.GONE) {
                if (labelShowedState == LabelsState.LEFT_VISIBLE) dX = Math.max(dX, labelWidth);
                if (labelShowedState == LabelsState.RIGHT_VISIBLE) dX = Math.min(dX, -labelWidth);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            } else {
                setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }

        if (labelShowedState == LabelsState.GONE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
        currentItemViewHolder = viewHolder;
    }

    private void setTouchListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                if (swipeBack) {
                    if (dX < -labelWidth) labelShowedState = LabelsState.RIGHT_VISIBLE;
                    else if (dX > labelWidth) labelShowedState = LabelsState.LEFT_VISIBLE;

                    if (labelShowedState != LabelsState.GONE) {
                        setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                }
                return false;
            }
        });
    }

    private void setTouchDownListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
                return false;
            }
        });
    }

    private void setTouchUpListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    SwipeController.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);
                    recyclerView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return false;
                        }
                    });
                    swipeBack = false;

                    labelShowedState = LabelsState.GONE;
                    currentItemViewHolder = null;
                }
                return false;
            }
        });
    }

    private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder, ChatAdapter adapter) {
        View itemView = viewHolder.itemView;

        float buttonWidthWithoutPadding = labelWidth - 20;
        float corners = 16;

        RectF label = null;
        Paint p = new Paint();
        p.setColor(Color.TRANSPARENT);

        if (labelShowedState == LabelsState.RIGHT_VISIBLE) {
            label = new RectF(itemView.getRight() - buttonWidthWithoutPadding, itemView.getTop(), itemView.getRight() - buttonWidthWithoutPadding / 2, itemView.getBottom());
            c.drawRoundRect(label, corners, corners, p);
            drawText(adapter.getMessage(viewHolder.getAdapterPosition()).getTimeAsString(), c, label, p);
        } else if (labelShowedState == LabelsState.LEFT_VISIBLE) {
            label = new RectF(itemView.getLeft() + buttonWidthWithoutPadding / 2, itemView.getTop(), itemView.getLeft() + buttonWidthWithoutPadding, itemView.getBottom());
            c.drawRoundRect(label, corners, corners, p);
            drawText(adapter.getMessage(viewHolder.getAdapterPosition()).getTimeAsString(), c, label, p);
        }
    }

    private void drawText(String text, Canvas c, RectF button, Paint p) {
        float textSize = 60;
        p.setColor(Color.BLACK);
        p.setAntiAlias(true);
        p.setTextSize(textSize);

        c.drawText(text, button.centerX() - (p.measureText(text) / 2), button.centerY() + (textSize / 2), p);
    }

    public void onDraw(Canvas c, ChatAdapter adapter) {
        if (currentItemViewHolder != null) {
            drawButtons(c, currentItemViewHolder, adapter);
        }
    }
}

