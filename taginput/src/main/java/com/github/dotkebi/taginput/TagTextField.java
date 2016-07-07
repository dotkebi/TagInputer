package com.github.dotkebi.taginput;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

/**
 * @author by dotkebi@gmail.com on 2016. 7. 7..
 */
public class TagTextField extends EditText {

    private int maxCountOfTags;
    private int maxLengthOfEachTags;

    public TagTextField(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public TagTextField(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public TagTextField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TagTextField(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            init(context);
        }
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.TagInputer, defStyleAttr, defStyleRes
        );

        try {
            maxCountOfTags = a.getInteger(R.styleable.TagInputer_maxCountOfTags, 0);
            maxLengthOfEachTags = a.getInteger(R.styleable.TagInputer_maxLengthOfEachTags, 0);
        } finally {
            a.recycle();
        }
        init(context);
    }

    private void init(Context context) {
        if (getText().length() == 0) {
            setText("");
            setSelection(0);
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused) {
            //doAfterChanged(getText());
        //} else if (getLastTag().equals(SHARP)) {
            //clearText();
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_BACK) {
            /*if (onBackKeyPressed != null) {
                onBackKeyPressed.onBackKeyPressed();
            }*/
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new TagInputConnection(super.onCreateInputConnection(outAttrs), true);
    }

    private class TagInputConnection extends InputConnectionWrapper {

        public TagInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                //removeFirstCharAtCursorPosition();
                return true;
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                //removeFirstCharAtCursorPosition();
                return true;
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }
}
