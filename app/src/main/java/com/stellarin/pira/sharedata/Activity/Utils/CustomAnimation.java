package com.stellarin.pira.sharedata.Activity.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;

import com.stellarin.pira.sharedata.R;

import is.arontibo.library.ElasticDownloadView;
import is.arontibo.library.IntroView;
import is.arontibo.library.ProgressDownloadView;

/**
 * Created by pira on 13/11/15.
 */
public class CustomAnimation extends ElasticDownloadView {
    /**
     * MARK: Constructor
     *
     * @param context
     * @param attrs
     */
    private IntroView mIntroView;
    private ProgressDownloadView mProgressDownloadView;
    public int mBackgroundColor;
    public CustomAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                is.arontibo.library.R.styleable.ColorOptionsView, 0, 0);
        mBackgroundColor = a.getColor(is.arontibo.library.R.styleable.ColorOptionsView_backgroundColor,
                getResources().getColor(android.R.color.holo_blue_dark));
        
    }

    /**
     * MARK: Overrides
     */

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mIntroView = (IntroView) findViewById(is.arontibo.library.R.id.intro_view);
        mIntroView.setListener(this);
        mProgressDownloadView = (ProgressDownloadView) findViewById(is.arontibo.library.R.id.progress_download_view);

        ViewTreeObserver vto = mProgressDownloadView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mProgressDownloadView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mProgressDownloadView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                mIntroView.getLayoutParams().width = mProgressDownloadView.getWidth();
                mIntroView.getLayoutParams().height = mProgressDownloadView.getHeight();

                mProgressDownloadView.setBackgroundColor(mBackgroundColor);
            }
        });
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);

        mIntroView.init();
        mIntroView.setVisibility(VISIBLE);
    }

    /**
     * MARK: Public methods
     */

    public void startIntro() {
        mIntroView.startAnimation();
    }

    public void setProgress(float progress) {
        mProgressDownloadView.setPercentage(progress);
    }

    public void success() {
        mProgressDownloadView.drawSuccess();
    }

    public void fail() {
        mProgressDownloadView.drawFail();
    }


    /**
     * MARK: Enter animation overrides
     */

    @Override
    public void onEnterAnimationFinished() {
        mIntroView.setVisibility(INVISIBLE);
        mProgressDownloadView.setVisibility(VISIBLE);
        mProgressDownloadView.setProgress(mProgressDownloadView.getProgress());

        // Do further actions if necessary
    }

}
