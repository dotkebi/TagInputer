package com.github.dotkebi.taginput;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.EditText;

import java.lang.ref.WeakReference;

/**
 * EditText with TagInput
 * @author by dotkebi@gmail.com on 2016-03-12.
 */
public class TagInput extends EditText {
    private static final int SET_SHARP = 7462;
    private static final int REMOVE_FIRST_CHAR_AT_CURSOR_POSITION = 7460;

    private static final long KEY_INTERVAL = 50;

    private static final char SHARP = '#';

    private TagInputHandler handler;

    private int previousCursorPosition;
    private int quantityOfPeriodBeforeCursor;

    private boolean blockSoftKey;
    private boolean hasFocus;

    public TagInput(Context context) {
        super(context);
        init(context);
    }

    public TagInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
       /* TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditDigits);
        if (a != null) {
            autoHideKeyboard = a.getBoolean(R.styleable.EditDigits_autoHideKeyboard, false);
            formatWhileInput = a.getBoolean(R.styleable.EditDigits_formatWhileInput, false);
            a.recycle();
        }

        if (autoHideKeyboard) {
            int type = this.getInputType();
            this.setInputType(InputType.TYPE_NULL);
            this.setRawInputType(type);
            this.setTextIsSelectable(true);
        }*/

        init(context);
    }

    private void init(Context context) {
        //this.context = context;
        hasFocus = false;

        handler = new TagInputHandler(this);
        addTextChangedListener(new TagWatcher());
    }

   /* @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN && !hasFocus) {
            *//*hasFocus = true;
            handler.sendEmptyMessageDelayed(BRING_CURSOR_TO_LAST_POSITION, 100);*//*
        }
        return super.onTouchEvent(event);
    }*/

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (!focused && hasFocus) {
            hasFocus = false;
            doAfterChanged(getText());
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            removeFirstCharAtCursorPosition();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setValue(float value) {
        doAfterChanged(String.valueOf(value));
    }

    public void setValue(int value) {
        doAfterChanged(String.valueOf(value));
    }

    private void removeFirstCharAtCursorPosition() {
        String text = getText().toString();
        recordCursorPosition(text);

        if (text.length() == 0) {
            return;
        }

        if (text.length() == 1) {
            clearText();
            return;
        }

        int startPosition = getSelectionStart() - 1;
        int endPosition = getSelectionStart();

        if (startPosition < 0) {
            return;
        }

        --previousCursorPosition;
        if (text.charAt(startPosition) == SHARP) {
            --startPosition;
            //--previousCursorPosition;
        }

        String front = text.substring(0, startPosition);
        String end = text.substring(endPosition, text.length());

        String message = front + end;
        sendSetText(message);
    }

    private void sendSetText(String value) {
        String str = value.replaceAll(String.valueOf(SHARP), "");
        if (TextUtils.isEmpty(str)) {
            clearText();
            return;
        }
        handler.sendMessage(Message.obtain(handler, SET_SHARP, str));
    }

    private void setSharp(String value) {
        blockSoftKey = true;
        setCursorVisible(false);
        try {
            if (TextUtils.isEmpty(value)) {
                return;
            }
            final int index = value.length();

            StringBuilder sb = new StringBuilder();

            sb.append(SHARP);
            ++previousCursorPosition;
            for (int i = 0; i < index; i++) {
                char ch = value.charAt(i);
                sb.append(ch);
                if (ch == ' ') {
                    sb.append(SHARP);
                    ++previousCursorPosition;
                }
            }
            clearText();
            String msg = sb.toString();
            setText(msg);

            previousCursorPosition -= quantityOfPeriodBeforeCursor;
            if (previousCursorPosition < 0) {
                previousCursorPosition = 0;
            } else if (previousCursorPosition > msg.length()) {
                previousCursorPosition = msg.length();
            }

            quantityOfPeriodBeforeCursor = 0;
            setSelection(previousCursorPosition);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            clearText();
        }
        setCursorVisible(true);
        blockSoftKey = false;
    }

    public void clearText() {
        if (getText().length() > 0) {
            getText().clear();
            setText(String.valueOf(SHARP));
            setSelection(1);
        }
    }

    private void doAfterChanged(Editable s) {
        doAfterChanged(s.toString());
    }

    private void doAfterChanged(String source) {
        if (blockSoftKey) {
            return;
        }

        recordCursorPosition(source);
        sendSetText(source);
    }

    private void recordCursorPosition(String s) {
        quantityOfPeriodBeforeCursor = 0;
        for (int i = 0; i < getSelectionStart(); i++) {
            if (s.charAt(i) == SHARP) {
                quantityOfPeriodBeforeCursor++;
            }
        }
        previousCursorPosition = getSelectionStart();
    }

    private class TagWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (blockSoftKey) {
                return;
            }

            if (s.length() == 1 && s.charAt(0) == SHARP) {
                return;
            }

            String str = s.toString();
            int lastSpace = str.lastIndexOf("# ");

            if (lastSpace > -1) {
                blockSoftKey = true;
                s.delete(s.length() - 1, s.length());
                blockSoftKey = false;
            }
            doAfterChanged(s);
        }
    }

    private static class TagInputHandler extends Handler {
        private final WeakReference<TagInput> weakBody;

        public TagInputHandler(TagInput klass) {
            weakBody = new WeakReference<>(klass);
        }

        @Override
        public void handleMessage(Message msg) {
            TagInput klass = weakBody.get();

            switch (msg.what) {
                case SET_SHARP:
                    String value = (String) msg.obj;
                    klass.setSharp(value);
                    break;

                case REMOVE_FIRST_CHAR_AT_CURSOR_POSITION:
                    klass.removeFirstCharAtCursorPosition();
                    sendEmptyMessageDelayed(REMOVE_FIRST_CHAR_AT_CURSOR_POSITION, KEY_INTERVAL);
                    break;
            }
        }
    }
}
