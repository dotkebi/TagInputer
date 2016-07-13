package com.github.dotkebi.taginput;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author by dotkebi@gmail.com on 2016. 7. 7..
 */
public class TagTextField extends EditText {

    private static final String SHARP = "#";

    private int maxCountOfTags;
    private int maxLengthOfEachTags;

    private int previousCursorPosition;
    private int quantityOfPeriodBeforeCursor;

    private TagTextFieldBridge tagTextFieldBridge;
    public void setTagTextFieldBridge(TagTextFieldBridge tagTextFieldBridge) {
        this.tagTextFieldBridge = tagTextFieldBridge;
    }

    private OnBackKeyPressed onBackKeyPressed;
    public void setOnBackKeyPressed(OnBackKeyPressed onBackKeyPressed) {
        this.onBackKeyPressed = onBackKeyPressed;
    }

    public TagTextField(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public TagTextField(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context, attrs, 0, 0);
        }
    }

    public TagTextField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context, attrs, defStyleAttr, 0);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TagTextField(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            init(context, attrs, defStyleAttr, defStyleRes);
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
            tagTextFieldBridge.sendTagTextField(getText());
            //doAfterChanged(getText());
        } else if (getLastTag().equals(SHARP)) {
            clearText();
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_BACK) {
            if (onBackKeyPressed != null) {
                onBackKeyPressed.onBackKeyPressed();
            }
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

    private void removeFirstCharAtCursorPosition() {
        String text = getText().toString();
        recordCursorPosition(text);

        if (text.length() == 0) {
            return;
        }

        if (text.length() == 1) {
            Log.i("current blank", getCaret(text, getSelectionStart()));
            clearTextWithSharp();
            return;
        }

        int startPosition = getSelectionStart() - 1;
        int endPosition = getSelectionStart();

        if (startPosition < 0) {
            return;
        }

        --previousCursorPosition;
        if (text.charAt(startPosition) == SHARP.charAt(0)) {
            --startPosition;
            //--previousCursorPosition;
        }

        String front = text.substring(0, startPosition);
        String end = text.substring(endPosition, text.length());

        String message = front + end;
        //Log.i("current caret", getCaret(message, previousCursorPosition));
        tagTextFieldBridge.sendTagTextField(message);
    }

    private void recordCursorPosition(String s) {
        quantityOfPeriodBeforeCursor = 0;
        for (int i = 0; i < getSelectionStart(); i++) {
            if (s.charAt(i) == SHARP.charAt(0)) {
                quantityOfPeriodBeforeCursor++;
            }
        }
        previousCursorPosition = getSelectionStart();
    }

    private String getCaret(String source, int position) {
        if (source.length() == 1) {
            tagTextFieldBridge.sendTagTextField("");
        }
        int endPositionOfCaret = source.lastIndexOf(" ", position);
        if (endPositionOfCaret == -1 || endPositionOfCaret < position) {
            endPositionOfCaret = source.length();
        }

        int startPositionOfCaret = 0;
        for (int i = endPositionOfCaret - 1; i > 1; i--) {
            char ch = source.charAt(i);
            if (ch == ' ') {
                startPositionOfCaret = i;
                break;
            }
        }
        String caret = source.substring(startPositionOfCaret, endPositionOfCaret).trim();
        tagTextFieldBridge.sendTagTextField(caret);
        return caret;
    }


    private List<String> getTags(String value) {
        return new ArrayList<>(Arrays.asList(value.split(" ")));
    }

    private String getLastTag(String value) {
        List<String> tags = getTags(value);
        return (tags.size() > 0) ? tags.get(tags.size() - 1) : "";
    }

    public String getLastTag() {
        return getLastTag(getText().toString());
    }

    public void clearTextWithSharp() {
        removeTextChangedListener(tagWatcher);
        setText(String.valueOf(SHARP));
        setSelection(1);
        addTextChangedListener(tagWatcher);
    }

    public void clearText() {
        removeTextChangedListener(tagWatcher);
        setText("");
        setSelection(0);
        addTextChangedListener(tagWatcher);
    }

    TextWatcher tagWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            tagTextFieldBridge.sendTagTextField(s);
        }
    };

    public void receiveTagResult(Editable s) {
        removeTextChangedListener(tagWatcher);
        setText(s);
        addTextChangedListener(tagWatcher);
    }

}
