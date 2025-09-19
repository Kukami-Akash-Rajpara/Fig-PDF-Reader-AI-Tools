package com.app.figpdfconvertor.figpdf.customwidget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.app.figpdfconvertor.figpdf.R;

public class DraggableTextView extends RelativeLayout {
    private EditText editText;
    private boolean isEditing = true;
    private float dX, dY; // drag offsets

    // --- Listener for edit mode changes ---
    public interface EditModeListener {
        void onEditEnabled(DraggableTextView textView);   // editing started
        void onEditDisabled(DraggableTextView textView);  // editing stopped
    }

    private EditModeListener listener;

    public void setEditModeListener(EditModeListener listener) {
        this.listener = listener;
    }

    public DraggableTextView(Context context) {
        super(context);
        init(context);
    }

    public DraggableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        editText = new EditText(context);
        editText.setHint("Type here");
        editText.setTextSize(24f);
        editText.setTextColor(Color.BLACK);
        editText.setBackgroundResource(R.drawable.dialog_background);
        editText.setGravity(Gravity.CENTER);
        editText.setPadding(50,20,20,50);
        editText.setSingleLine(false);
        editText.setHorizontallyScrolling(false);

        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_IN_PARENT, TRUE);
        addView(editText, params);

        // Click inside EditText to re-enable editing if locked
        editText.setOnClickListener(v -> {
            if (!isEditing) enableEditing();
        });
    }

    // --- Enable text editing ---
    public void enableEditing() {
        isEditing = true;
        editText.setFocusableInTouchMode(true);
        editText.setCursorVisible(true);
        editText.requestFocus();
        editText.setBackgroundResource(R.drawable.dialog_background);

        // Remove drag listener while editing
        editText.setOnTouchListener(null);

        if (listener != null) listener.onEditEnabled(this);
    }

    // --- Disable text editing but allow drag ---
    public void disableEditing() {
        isEditing = false;
        editText.setFocusable(false);
        editText.setCursorVisible(false);
        editText.clearFocus();
        editText.setBackground(null);

        // Variables for detecting click vs drag
        final long[] downTime = {0};
        final float[] startX = {0};
        final float[] startY = {0};

        editText.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downTime[0] = System.currentTimeMillis();
                    startX[0] = event.getRawX();
                    startY[0] = event.getRawY();
                    dX = getX() - startX[0];
                    dY = getY() - startY[0];
                    break;

                case MotionEvent.ACTION_MOVE:
                    setX(event.getRawX() + dX);
                    setY(event.getRawY() + dY);
                    break;

                case MotionEvent.ACTION_UP:
                    long duration = System.currentTimeMillis() - downTime[0];
                    float deltaX = Math.abs(event.getRawX() - startX[0]);
                    float deltaY = Math.abs(event.getRawY() - startY[0]);

                    // If finger didn't move much → treat as click
                    if (duration < 200 && deltaX < 10 && deltaY < 10) {
                        enableEditing();
                    }
                    break;
            }
            return true; // consume touch
        });

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }

        if (listener != null) listener.onEditDisabled(this);
    }

    // --- Fully lock text (no drag, no edit) ---
    public void lock() {
        isEditing = false;

        // Disable dragging
        setOnTouchListener(null);

        // Disable text editing
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setCursorVisible(false);
        editText.setLongClickable(false);
        editText.setKeyListener(null); // removes typing ability

        // Prevent clicks opening keyboard
        editText.setOnClickListener(null);
        editText.setOnTouchListener((v, event) -> true); // consume touches

        editText.clearFocus();
    }

    // --- Font controls ---
    public void setFontSize(float sizeInSp) {
        float scaledSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sizeInSp,
                getResources().getDisplayMetrics()
        );
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledSize);
    }

    public void setFontColor(int color) {
        editText.setTextColor(color);
    }

    public int getFontColor() {
        return editText.getCurrentTextColor();
    }

    public float getFontSize() {
        return editText.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
    }

    public String getText() {
        return editText.getText().toString();
    }
}