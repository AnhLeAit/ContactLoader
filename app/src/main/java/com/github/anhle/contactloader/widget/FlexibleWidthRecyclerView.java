/*
 * Copyright (C) 2017 by Lê Ngọc Anh(Tom Le) - Email: anhle.ait@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.anhle.contactloader.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.github.anhle.contactloader.utils.AppUtils;


/**
 * Created by AnhLe on 5/12/17.
 */

public class FlexibleWidthRecyclerView extends RecyclerView {

    private int maxContentWidth;

    public FlexibleWidthRecyclerView(Context context) {
        super(context);
        init();
    }

    public FlexibleWidthRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init();
        }
    }

    public FlexibleWidthRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            init();
        }
    }

    private void init() {
        // Max content with must be 2/3 of screen width.
        maxContentWidth = AppUtils.getScreenWidth(getContext()) * 2 / 3;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int width = getMeasuredWidth();
        int contentWidth = 0;
        View child = this.getChildAt(0);

        if (child != null) {
            contentWidth = child.getWidth() * getChildCount();
        }

        // When the content width lager than 2/3 screen width
        if (contentWidth > maxContentWidth) {
            int height = getMeasuredHeight();

            // The width must be set 2/3 parent's width.
            int parentWidth = ((View) getParent()).getWidth();
            setMeasuredDimension(parentWidth * 2 / 3, height);

            // Show over scroll effect.
            setOverScrollMode(OVER_SCROLL_ALWAYS);
        } else {
            // Disable over scroll effect.
            setOverScrollMode(OVER_SCROLL_NEVER);
        }
    }
}
