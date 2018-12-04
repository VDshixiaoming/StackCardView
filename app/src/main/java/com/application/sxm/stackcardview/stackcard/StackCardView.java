package com.application.sxm.stackcardview.stackcard;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.application.sxm.stackcardview.R;


/**
 * 轮播动效的折叠卡片
 * Created by shixiaoming on 18/11/27.
 */
public class StackCardView extends FrameLayout {

    private static final String TAG = "StackView";
    private BaseAdapter mAdapter;
    private DataSetObserver mDataSetObserver;
    private int mStartIndex;
    private boolean mIsAnimation;//是否在执行动画
    private SparseArray<View> mCacheView = new SparseArray<>();
    private StackRunnable mRunnable;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Context mContext;
    private CardConfig CardConfig;
    private boolean autoPlay = false;

    public StackCardView(Context context) {
        this(context, null);
    }

    public StackCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StackCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        CardConfig = new CardConfig();
    }

    public class CardConfig {
        public int MAX_VISIBLE_COUNT = 3;
        public int BASE_TRANSLATION_X = dip2px(mContext, 18);
        public float BASE_SCALE = 0.09F;
    }

    public void setAdapter(BaseAdapter adapter) {
        mAdapter = adapter;
        if (mAdapter != null && mDataSetObserver != null) {
            try {
                mAdapter.unregisterDataSetObserver(mDataSetObserver);
            } catch (Exception e) {

            }
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mDataSetObserver = new StackDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
        requestLayout();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //重新定义父View的高度，否则会导致子View被遮挡;
        float parentWidth;
        float scaleWidth = 0;
        int childWidth = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View childView = getChildAt(i);
            if (childView != null) {
                scaleWidth += (childView.getTranslationX() * childView.getScaleX());
                if (i == getChildCount() - 1) {//只需测量第一个高度即可
                    childWidth = childView.getWidth();
                }
            }
        }
        parentWidth = childWidth + scaleWidth;
        setMeasuredDimension((int) parentWidth, MeasureSpec.getSize(heightMeasureSpec));

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (getChildCount() == 0) {
            initView();
        }
    }

    //初始化布局
    public void initView() {
        int itemCount = mAdapter.getCount();

        int layoutCount = Math.min(itemCount, CardConfig.MAX_VISIBLE_COUNT);
        int viewLevel;//View的层级
        boolean flag = layoutCount > 1;

        for (int pos = 0; flag ? pos <= layoutCount : pos < layoutCount; pos++) {//比最大可见数量多布局一个，动画更顺畅
            mStartIndex = pos % itemCount;
            View childView = mAdapter.getView(mStartIndex, null, this);
            viewLevel = getChildCount();
            if (pos == layoutCount) {
                viewLevel = pos - 1;
            }
            childView.setTranslationX(viewLevel * CardConfig.BASE_TRANSLATION_X);
            childView.setScaleX(1 - viewLevel * CardConfig.BASE_SCALE);
            childView.setScaleY(1 - viewLevel * CardConfig.BASE_SCALE);
            addViewInLayout(childView, 0, childView.getLayoutParams());
            childView.requestLayout();
        }
    }


    public void exitWithAnimation() {
        for (int index = getChildCount() - 1; index >= 0; index--) {
            View childView = getChildAt(index);
            if (index == getChildCount() - 1) {//第一个view
                ObjectAnimator alpha = ObjectAnimator.ofFloat(childView, "alpha", 1f, 0f);
                alpha.setDuration(500);
                alpha.start();
                alpha.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        View deleteView = getChildAt(getChildCount() - 1);
                        mCacheView.put(R.id.imageView, deleteView);//讲移除掉的view进行缓存，避免每次创建,
                        // 由于每个View的类型是一样的，所以以同一个id为key即可，否则将根据viewType类型为key进行缓存
                        removeViewInLayout(deleteView);
                        makeView();
                        mHandler.postDelayed(mRunnable, 4000);//循环

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            } else if (index == 0) {//最后一个view不动

            } else {

                View lastView = getChildAt(index + 1);

                ObjectAnimator scaleX = ObjectAnimator.ofFloat(childView, "scaleX", childView.getScaleX(), lastView.getScaleX());
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(childView, "scaleY", childView.getScaleY(), lastView.getScaleY());
                ObjectAnimator translateY = ObjectAnimator.ofFloat(childView, "translationX", childView.getTranslationX(), lastView.getTranslationX());
                AnimatorSet set = new AnimatorSet();
                set.play(scaleX).with(scaleY).with(translateY);
                set.setDuration(500);
                set.start();
            }
        }
    }

    private void makeView() {
        mStartIndex++;
        if (mStartIndex == mAdapter.getCount()) {
            mStartIndex = 0;
        }
        View covertView = mCacheView.get(R.id.imageView);//缓存的消失的view
        mAdapter.getView(mStartIndex, covertView, this);
        covertView.setAlpha(1);//恢复状态，否则alpha还是0;
        int level = getChildCount() - 1;
        covertView.setTranslationX(level * CardConfig.BASE_TRANSLATION_X);
        covertView.setScaleX(1 - (level * CardConfig.BASE_SCALE));
        covertView.setScaleY(1 - (level * CardConfig.BASE_SCALE));
        addView(covertView, 0, covertView.getLayoutParams());
    }

    public void stopLoop() {
        if (mRunnable == null) {
            return;
        }
        mHandler.removeCallbacks(mRunnable);
        mRunnable = null;
        mIsAnimation = false;
    }

    public void startAutoPlay () {
        this.autoPlay = true;
        startLoop();
    }

    public void startLoop() {

        if (mRunnable != null || mIsAnimation) {
            return;
        }
        mRunnable = new StackRunnable();
        exitWithAnimation();
        mIsAnimation = true;
    }

    @Override
    protected void onDetachedFromWindow() {//view从屏幕移除时候及时销毁动画
        super.onDetachedFromWindow();
        stopLoop();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!autoPlay) {
            return;
        }
        startLoop();
    }

    private class StackRunnable implements Runnable {

        @Override
        public void run() {
            exitWithAnimation();
        }
    }

    private class StackDataSetObserver extends DataSetObserver {

        public StackDataSetObserver() {
            super();
        }

        @Override
        public void onChanged() {
            removeAllViewsInLayout();
            requestLayout();
            super.onChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    }


    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FrameLayout.LayoutParams(mContext, attrs);
    }

    public static int dip2px(Context context, float dpValue) {

        final float scale = context.getResources().getDisplayMetrics().density;

        return (int) (dpValue * scale + 0.5f);

    }

}
