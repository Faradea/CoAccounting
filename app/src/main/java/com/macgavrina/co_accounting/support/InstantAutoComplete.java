package com.macgavrina.co_accounting.support;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

@SuppressLint("AppCompatCustomView")
public class InstantAutoComplete extends AutoCompleteTextView {

    public InstantAutoComplete(Context context) {
        super(context);
    }

    public InstantAutoComplete(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public InstantAutoComplete(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
//        if (focused && getAdapter() != null) {
//            performFiltering(getText(), 0);
//        }
        performFiltering("", 0);
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    public void forceFiltering() {
        performFiltering("", 0);
        showDropDown();
    }

}
