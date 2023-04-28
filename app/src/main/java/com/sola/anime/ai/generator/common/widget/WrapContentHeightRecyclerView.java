package com.sola.anime.ai.generator.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public class WrapContentHeightRecyclerView extends RecyclerView {
    public WrapContentHeightRecyclerView(Context context) {
        super(context);
    }

    public WrapContentHeightRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapContentHeightRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpecCustom = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpecCustom);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}