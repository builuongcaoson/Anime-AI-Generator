package com.sola.anime.ai.generator.common.widget.cardSlider;

import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;

import com.google.android.material.card.MaterialCardView;

public class CardsUpdater extends DefaultViewUpdater {

    @Override
    public void updateView(@NonNull View view, float position) {
        super.updateView(view, position);

        final LinearLayout card = ((LinearLayout)view);
        final View alphaView = card.getChildAt(1);
        final View imageView = card.getChildAt(0);

        if (position < 0) {
            final float alpha = ViewCompat.getAlpha(view);
            ViewCompat.setAlpha(view, 1f);
            ViewCompat.setAlpha(alphaView, 0.9f - alpha);
            ViewCompat.setAlpha(imageView, 0.3f + alpha);
        } else {
            ViewCompat.setAlpha(alphaView, 0f);
            ViewCompat.setAlpha(imageView, 1f);
        }

    }

}
