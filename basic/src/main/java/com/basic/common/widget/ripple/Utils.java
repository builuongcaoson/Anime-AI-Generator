package com.basic.common.widget.ripple;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.animation.ArgbEvaluatorCompat;
import com.widget.R;

import java.util.Arrays;

public class Utils {
    
    public static final int alpha = 70;
    
    static void animateBackground(int endColor, View view) {
        view.clearAnimation();
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluatorCompat(),
                view.getBackgroundTintList().getDefaultColor(),
                endColor);
        valueAnimator.setDuration(300L);
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        valueAnimator.addUpdateListener(animation -> view.setBackgroundTintList(ColorStateList.valueOf((int) animation.getAnimatedValue())));
        valueAnimator.start();
    }
    
    public static void animateBackground(int endColor, ViewGroup view) {
        view.clearAnimation();
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluatorCompat(),
                view.getBackgroundTintList().getDefaultColor(),
                endColor);
        valueAnimator.setDuration(300L);
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        valueAnimator.addUpdateListener(animation -> view.setBackgroundTintList(ColorStateList.valueOf((int) animation.getAnimatedValue())));
        valueAnimator.start();
    }
    
    static RippleDrawable getRippleDrawable(Context context, Drawable backgroundDrawable,  @ColorRes Integer color) {
        float[] outerRadii = new float[8];
        float[] innerRadii = new float[8];
        Arrays.fill(outerRadii, 60);
        Arrays.fill(innerRadii, 60);
        
        RoundRectShape shape = new RoundRectShape(outerRadii, null, innerRadii);
        ShapeDrawable mask = new ShapeDrawable(shape);
        
        ColorStateList stateList = ColorStateList.valueOf(ContextCompat.getColor(context, color));
        
        RippleDrawable rippleDrawable = new RippleDrawable(stateList, backgroundDrawable, mask);
        rippleDrawable.setAlpha(alpha);
        
        return rippleDrawable;
    }

    static RippleDrawable getRippleDrawable(Context context, Drawable backgroundDrawable, float divisiveFactor, @ColorInt Integer color) {
        float[] outerRadii = new float[8];
        float[] innerRadii = new float[8];
        Arrays.fill(outerRadii, 60 / divisiveFactor);
        Arrays.fill(innerRadii, 60 / divisiveFactor);

        RoundRectShape shape = new RoundRectShape(outerRadii, null, innerRadii);
        ShapeDrawable mask = new ShapeDrawable(shape);

        ColorStateList stateList = ColorStateList.valueOf(color);

        RippleDrawable rippleDrawable = new RippleDrawable(stateList, backgroundDrawable, mask);
        rippleDrawable.setAlpha(alpha);

        return rippleDrawable;
    }
    
    static RippleDrawable getRippleDrawableRes(Context context, Drawable backgroundDrawable, float divisiveFactor, @ColorRes Integer color) {
        float[] outerRadii = new float[8];
        float[] innerRadii = new float[8];
        Arrays.fill(outerRadii, 60 / divisiveFactor);
        Arrays.fill(innerRadii, 60 / divisiveFactor);
        
        RoundRectShape shape = new RoundRectShape(outerRadii, null, innerRadii);
        ShapeDrawable mask = new ShapeDrawable(shape);
        
        ColorStateList stateList = ColorStateList.valueOf(ContextCompat.getColor(context, color));
        
        RippleDrawable rippleDrawable = new RippleDrawable(stateList, backgroundDrawable, mask);
        rippleDrawable.setAlpha(alpha);
        
        return rippleDrawable;
    }
}
