package com.github.dotkebi.taginput;

import android.text.Editable;
import android.text.SpannableStringBuilder;

/**
 * @author by dotkebi on 2016. 7. 14..
 */
public class TagTextDelegate implements TagTextFieldBridge {

    private TagTextField tagTextField;

    public TagTextDelegate(TagTextField tagTextField) {
        this.tagTextField = tagTextField;
    }


    @Override
    public void sendTagTextField(Editable tagTextField) {
        check(tagTextField.toString());
    }

    @Override
    public void sendTagTextField(String tagTextField) {
        check(tagTextField);
    }

    private void check(String string) {

        Editable editable = new SpannableStringBuilder(string);

        tagTextField.receiveTagResult(editable);

    }


}
