package com.github.dotkebi.taginput;

import android.text.Editable;

/**
 * @author by dotkebi on 2016. 7. 7..
 */
public interface TagTextFieldBridge {
    void sendTagTextField(Editable tagTextField);
    void sendTagTextField(String tagTextField);
}
