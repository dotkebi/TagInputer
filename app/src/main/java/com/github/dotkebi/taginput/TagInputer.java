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
import android.view.KeyEvent;
import android.widget.EditText;

import java.lang.ref.WeakReference;

/**
 * EditText with TagInput
 * @author by dotkebi@gmail.com on 2016-03-12.
 */
public class TagInputer extends EditText {
    private static final int SET_SHARP = 7462;
    private static final int REMOVE_FIRST_CHAR_AT_CURSOR_POSITION = 7460;

    private static final long KEY_INTERVAL = 50;

    private static final String SHARP = "#";

    private TagInputHandler handler;

    private int previousCursorPosition;
    private int quantityOfPeriodBeforeCursor;

    private boolean blockSoftKey;
    private boolean hasFocus;
    private OnInputTagListener onInputTagListener;

    public TagInputer(Context context) {
        super(context);
        init(context);
    }

    public TagInputer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setOnInputTagListener(OnInputTagListener onInputTagListener) {
        this.onInputTagListener = onInputTagListener;
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
        hasFocus = false;

        handler = new TagInputHandler(this);

        if (getText().length() == 0) {
            clearText();
        }
        /*if (onInputTagListener == null) {
            onInputTagListener = new OnInputTagListener() {
                @Override
                public void onInputTagListener(String[] tags) {
                    for (String str : tags) {
                        Log.d("tags", str);
                    }
                }
            };
        }*/
        addTextChangedListener(new TagWatcher());
    }

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

    public String[] getTags() {
        return getText().toString().replaceAll(SHARP, "").split(" ");
    }

    public void setValue(String value) {
        doAfterChanged(value);
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
        if (text.charAt(startPosition) == SHARP.charAt(0)) {
            --startPosition;
            //--previousCursorPosition;
        }

        String front = text.substring(0, startPosition);
        String end = text.substring(endPosition, text.length());

        String message = front + end;
        sendSetText(message);
    }

    private String sendToListener(String value) {
        String str = value.replaceAll(SHARP, "");
        if (onInputTagListener != null) {
            onInputTagListener.onInputTagListener(str.split(" "));
        }
        return str;
    }

    private void sendSetText(String value) {
        /*if (TextUtils.isEmpty(str)) {
            clearText();
            return;
        }*/
        handler.sendMessage(Message.obtain(handler, SET_SHARP, sendToListener(value)));
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
        getText().clear();
        setText(String.valueOf(SHARP));
        setSelection(1);
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
            if (s.charAt(i) == SHARP.charAt(0)) {
                quantityOfPeriodBeforeCursor++;
            }
        }
        previousCursorPosition = getSelectionStart();
    }

    public interface OnInputTagListener {
        void onInputTagListener(String[] tags);
    }

    private static class TagInputHandler extends Handler {
        private final WeakReference<TagInputer> weakBody;

        public TagInputHandler(TagInputer klass) {
            weakBody = new WeakReference<>(klass);
        }

        @Override
        public void handleMessage(Message msg) {
            TagInputer klass = weakBody.get();

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

            if (s.length() == 1 && s.charAt(0) == SHARP.charAt(0)) {
                return;
            }

            String str = s.toString();
            // block duplicated space
            int lastSpace = str.lastIndexOf("# ");
            // block duplicated #
            int lastSharp = str.lastIndexOf("##");

            if (lastSpace > -1 || lastSharp > -1) {
                blockSoftKey = true;
                s.delete(s.length() - 1, s.length());
                blockSoftKey = false;
                return;
            }

            if (s.length() < 1) {
                return;
            }

            int firstSharp = str.indexOf("#");
            lastSharp = str.lastIndexOf("#");

            if (lastSharp > -1 && firstSharp != lastSharp) {
                if (s.charAt(lastSharp - 1) != ' ') {
                    blockSoftKey = true;
                    s.delete(s.length() - 1, s.length());
                    s.append(" " + SHARP);
                    sendToListener(s.toString());
                    blockSoftKey = false;
                    return;
                }
            }

            if (s.charAt(s.length() - 1) == ' ') {
                doAfterChanged(s);
            }
        }
    }
}
