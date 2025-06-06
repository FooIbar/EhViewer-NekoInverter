/*
 * Copyright (C) 2015 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer.widget;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ContentInfo;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatEditText;

import com.hippo.util.ExceptionUtils;

public class SearchEditText extends AppCompatEditText {
    private SearchEditTextListener mListener;

    public SearchEditText(Context context) {
        super(context);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSearchEditTextListener(SearchEditTextListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // special case for the back key, we do not even try to send it
            // to the drop down list but instead, consume it immediately
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.handleUpEvent(event);
                }
                if (event.isTracking() && !event.isCanceled()) {
                    // TODO stopSelectionActionMode
                    if (mListener != null) {
                        mListener.onBackPressed();
                        return true;
                    }
                }
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && mListener != null) {
            mListener.onClick();
        }
        try {
            return super.onTouchEvent(event);
        } catch (Throwable t) {
            // Some devices crash here.
            // I don't why.
            ExceptionUtils.INSTANCE.throwIfFatal(t);
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Nullable
    @Override
    public ContentInfo onReceiveContent(@NonNull ContentInfo payload) {
        ClipData clipData = payload.getClip();
        if (clipData.getItemCount() == 1) {
            if (mListener != null) {
                mListener.onReceiveContent(clipData.getItemAt(0).getUri());
            }
        }
        return super.onReceiveContent(payload);
    }

    public interface SearchEditTextListener {
        void onClick();

        void onBackPressed();

        void onReceiveContent(Uri uri);
    }
}
