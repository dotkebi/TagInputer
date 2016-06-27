package com.github.dotkebi.taginput;

import android.view.View;

/**
 * @author by dotkebi@gmail.com on 2016. 5. 6..
 */
public interface OnTagListener {

    void onLastTagListener(View view, String tags);

    /**
     * @author by dotkebi@gmail.com on 2016. 5. 12..
     */
    interface OnLastInputTagListener {
        void onLastInputTagListener(String tags);
    }

}
