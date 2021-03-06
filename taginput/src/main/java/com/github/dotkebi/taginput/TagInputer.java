package com.github.dotkebi.taginput;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EditText with TagInput
 * @author by dotkebi@gmail.com on 2016-03-12.
 */
public class TagInputer extends EditText {
    private static final int SET_SHARP = 7462;
    private static final int REMOVE_FIRST_CHAR_AT_CURSOR_POSITION = 7460;

    private static final long KEY_INTERVAL = 50;

    private static final String SHARP = "#";

    private int previousCursorPosition;
    private int quantityOfPeriodBeforeCursor;

    private int maxCountOfTags;
    private int maxLengthOfEachTags;

    private boolean hasFocus;

    private OnCurrentTagListener onCurrentTagListener;
    public void setOnCurrentTagListener(OnCurrentTagListener onCurrentTagListener) {
        this.onCurrentTagListener = onCurrentTagListener;
    }

    private OnLastInputTagListener onLastInputTagListener;
    public void setOnLastInputTagListener(OnLastInputTagListener onLastInputTagListener) {
        this.onLastInputTagListener = onLastInputTagListener;
    }

    private OnBackKeyPressed onBackKeyPressed;
    public void setOnBackKeyPressed(OnBackKeyPressed onBackKeyPressed) {
        this.onBackKeyPressed = onBackKeyPressed;
    }

    public TagInputer(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public TagInputer(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context, attrs, 0, 0);
        }
    }

    public TagInputer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context, attrs, defStyleAttr, 0);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TagInputer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        addTextChangedListener(tagWatcher);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused) {
            doAfterChanged(getText());
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
                removeFirstCharAtCursorPosition();
                return true;
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                removeFirstCharAtCursorPosition();
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
        Log.i("current caret", getCaret(message, previousCursorPosition));
        sendToText(message);
    }

    private String sendToListenerAndRemoveSharp(String value) {
        if (onLastInputTagListener != null) {
            onLastInputTagListener.onLastInputTagListener(getLastTag(value));
        }
        return value.replaceAll(SHARP, "");
    }

    private void sendToText(String value) {
        handler.sendMessage(Message.obtain(handler, SET_SHARP, sendToListenerAndRemoveSharp(value)));
    }

    private void setSharp(String value) {
        if (TextUtils.isEmpty(value)) {
            clearTextWithSharp();
            return;
        }

        try {
            removeTextChangedListener(tagWatcher);
            setCursorVisible(false);

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
            String msg = sb.toString();
            Log.w("test", String.format("(%s)", msg));
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
            clearTextWithSharp();
        }
        setCursorVisible(true);
        addTextChangedListener(tagWatcher);
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

    private void doAfterChanged(Editable s) {
        doAfterChanged(s.toString());
    }

    private void doAfterChanged(String source) {
        recordCursorPosition(source);
        sendToText(source);
    }

    private String getCaret(String source, int position) {
        if (source.length() == 1) {
            if (onCurrentTagListener != null) {
                onCurrentTagListener.onCurrentTagListener("");
            }
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
        if (onCurrentTagListener != null) {
            onCurrentTagListener.onCurrentTagListener(caret);
        }
        return caret;
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

    private TagInputHandler handler = new TagInputHandler(this);

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

    TextWatcher tagWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && s.charAt(0) == SHARP.charAt(0)) {
                Log.i("current blank", getCaret(s.toString(), getSelectionStart()));
                return;
            }

            String str = s.toString();
            // block duplicated space
            int lastSpace = str.lastIndexOf("# ");
            // block duplicated #
            int lastSharpDuplicated = str.lastIndexOf("##");

            if (lastSpace > -1 || lastSharpDuplicated > -1) {
                removeTextChangedListener(tagWatcher);
                s.delete(s.length() - 1, s.length());
                Log.i("current caret", getCaret(s.toString(), getSelectionStart()));
                addTextChangedListener(tagWatcher);
                return;
            }

            if (s.length() < 1) {
                return;
            }

            // limit length of tags
            /*String buffer = "";
            boolean flag = false;
            recordCursorPosition(str);
            for (String item : getTags(str)) {
                if (item.length() - 1 > maxLengthOfEachTags
                        && maxLengthOfEachTags > 0) {
                    item = item.substring(0, item.length() - 1);
                    flag = true;
                }
                buffer = addToBuffer(buffer, item);
            }
            if (flag) {
                --previousCursorPosition;
                sendToText(buffer);
            }*/

            int firstSharp = str.indexOf(SHARP);
            lastSharpDuplicated = str.lastIndexOf(SHARP);

            if (lastSharpDuplicated > -1 && firstSharp != lastSharpDuplicated) {
                if (s.charAt(lastSharpDuplicated - 1) != ' ') {
                    removeTextChangedListener(tagWatcher);
                    s.delete(s.length() - 1, s.length());
                    s.append(" " + SHARP);
                    sendToListenerAndRemoveSharp(s.toString());
                    Log.i("current caret lastSharp", getCaret(s.toString(), getSelectionStart()));
                    addTextChangedListener(tagWatcher);
                    return;
                }
            }

            // limit count of tags
            if (s.charAt(s.length() - 1) == ' ') {
                if (countOfSubString(str, SHARP) >= maxCountOfTags
                        && maxCountOfTags > 0) {
                    removeTextChangedListener(tagWatcher);
                    s.delete(s.length() - 1, s.length());
                    sendToListenerAndRemoveSharp(s.toString());
                    Log.i("current caret limit", getCaret(s.toString(), getSelectionStart()));
                    addTextChangedListener(tagWatcher);
                    return;
                }
                doAfterChanged(s);
            }

            Log.i("current caret ", getCaret(s.toString(), getSelectionStart()));
        }
    };

    /*private String addToBuffer(String buffer, String item) {
        return (TextUtils.isEmpty(buffer)) ? buffer + item : buffer + " " + item;
    }*/

    private int countOfSubString(String where, String find) {
        Pattern pattern = Pattern.compile(find);
        Matcher matcher = pattern.matcher(where);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private List<String> getTags(String value) {
        return new ArrayList<>(Arrays.asList(value.split(" ")));
    }

    private String getLastTag(String value) {
        List<String> tags = getTags(value);
        return (tags.size() > 0) ? tags.get(tags.size() - 1) : "";
    }

    /**
     * public methods
     */

    /**
     * getLastTag
     */
    public String getLastTag() {
        return getLastTag(getText().toString());
    }

    public boolean hasTags() {
        return getTags().size() > 0 && !getLastTag().equals(SHARP);
    }

    public List<String> getTags() {
        return getTags(getText().toString());
    }

    public CharSequence getTagsWithComma() {
        return getText().toString().replaceAll(" ", ",");
    }

    public void addTag(CharSequence charSequence) {
        if (maxCountOfTags < 0) {
            return;
        }
        if (countOfSubString(getText().toString(), SHARP) >= maxCountOfTags) {
            return;
        }
        removeTextChangedListener(tagWatcher);
        if (!TextUtils.isEmpty(getLastTag().replaceAll(SHARP, ""))) {
            append(" #");
        }
        append(charSequence);
        addTextChangedListener(tagWatcher);
    }

    public void setMaxCountOfTags(int maxCountOfTags) {
        this.maxCountOfTags = maxCountOfTags;
    }

    public void setMaxLengthOfEachTags(int maxLengthOfEachTags) {
        this.maxLengthOfEachTags = maxLengthOfEachTags;
    }
}
