package com.saif.multifloatingactionbar;

import android.animation.Animator;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class MultiFloatingActionBar extends LinearLayout
{
    // todo One function remains it's the Direction of the Children show:
    private boolean isChildBtnVisible;

    public MultiFloatingActionBar(Context context) {
        super(context);
        init();
    }

    public MultiFloatingActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init()
    {
        isChildBtnVisible = false;
    }

    @Override
    public void onViewAdded(View child) {
        child.setVisibility(INVISIBLE);
        super.onViewAdded(child);
    }


    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        View child = getChildAt(getChildCount()-1);
        child.setVisibility(VISIBLE);
        child.setOnClickListener(mainBtnListener);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);

        View main_child = getChildAt(getChildCount()-1);
        MarginLayoutParams main_params = (MarginLayoutParams) main_child.getLayoutParams();
        int main_btn_dim;
        int main_margin;

        if (getOrientation()==VERTICAL)
        {
            main_btn_dim = main_child.getWidth();
            main_margin = main_params.leftMargin + main_params.rightMargin;
        }
        else
        {
            main_btn_dim = main_child.getHeight();
            main_margin = main_params.topMargin + main_params.bottomMargin;
        }

        for (int i=0; i<getChildCount()-1;i++ )
        {
            View child = getChildAt(i);
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();

            if (child.getWidth()>main_btn_dim && getOrientation()==VERTICAL)
                params.bottomMargin = 16;
            else if (child.getHeight()>main_btn_dim && getOrientation()==HORIZONTAL)
                params.rightMargin = 16;
            else
            {
                int margin = Math.abs((main_btn_dim+main_margin)-child.getWidth())/2;
                if (getOrientation()==VERTICAL) params.setMargins(margin, 0, margin, 16);
                else params.setMargins(0, margin, 16, margin);
            }

            child.setLayoutParams(params);
        }
    }

    private OnClickListener mainBtnListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (isChildBtnVisible)
                hideSubBtn();
            else
                showSubBtn();
        }
    };

    private void showSubBtn()
    {
        isChildBtnVisible=true;
        View main_child = getChildAt(getChildCount()-1);
        float position_main_btn = main_child.getY();
        main_child.animate().rotation(180).setDuration(400)
                .setInterpolator(new LinearInterpolator()).start();

        for (int i=0; i<getChildCount()-1; i++)
        {
            final View child = getChildAt(i);
            child.setTranslationY(position_main_btn);
            child.setVisibility(VISIBLE);
            child.setAlpha(0);
            child.animate().setDuration(400).alpha(1).translationY(0)
                    .setInterpolator(new LinearInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) { }
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            child.setVisibility(VISIBLE);
                        }
                        @Override
                        public void onAnimationCancel(Animator animation) { }
                        @Override
                        public void onAnimationRepeat(Animator animation) { }
                    })
                    .start();
        }
    }

    private void hideSubBtn()
    {
        isChildBtnVisible=false;
        View main_child = getChildAt(getChildCount()-1);
        float position_main_btn = main_child.getY();
        main_child.animate().rotation(0).setDuration(400)
                .setInterpolator(new LinearInterpolator()).start();

        for (int i=0; i<getChildCount()-1; i++)
        {
            final View child = getChildAt(i);
            child.animate().setDuration(400).alpha(0).translationY(position_main_btn)
                    .setInterpolator(new LinearInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) { }
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            child.setVisibility(INVISIBLE);
                        }
                        @Override
                        public void onAnimationCancel(Animator animation) { }
                        @Override
                        public void onAnimationRepeat(Animator animation) { }
                    })
                    .start();
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState()
    {
        SaveState state = new SaveState(super.onSaveInstanceState());
        state.setChildrenVisible(isChildBtnVisible);
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        SaveState saveState = (SaveState) state;
        super.onRestoreInstanceState(saveState.getSuperState());

        isChildBtnVisible = saveState.isChildrenVisible();
        if (isChildBtnVisible)
        {
            getChildAt(getChildCount()-1).setRotation(180);

            for (int i=0; i<getChildCount()-1; i++)
                getChildAt(i).setVisibility(VISIBLE);
        }
    }


    private static class SaveState extends BaseSavedState
    {
        private boolean isChildrenVisible;

        SaveState(Parcelable superState) {
            super(superState);
        }

        private SaveState(Parcel source) {
            super(source);
            isChildrenVisible = source.readByte()==1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (isChildrenVisible?1:0));
        }

        public static final Creator<SaveState> CREATOR = new
                Creator<SaveState>(){
            @Override
            public SaveState createFromParcel(Parcel source) {
                return new SaveState(source);
            }
            @Override
            public SaveState[] newArray(int size) {
                return new SaveState[size];
            }
        };

        public boolean isChildrenVisible() {
            return isChildrenVisible;
        }

        public void setChildrenVisible(boolean childrenVisible) {
            isChildrenVisible = childrenVisible;
        }
    }
}
