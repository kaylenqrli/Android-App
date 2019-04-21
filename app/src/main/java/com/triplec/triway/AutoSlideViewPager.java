package com.triplec.triway;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Auto scrolling and recycling ViewPager
 * Source: https://www.jianshu.com/p/58f356eaa6e9
 * @param <T>
 */
public class AutoSlideViewPager<T extends PagerAdapter> extends FrameLayout {
    private ViewPager viewPager;
    private PagerAdapter mAdapter;
    private LinearLayout mLinearLayout;
    private Context mContext;

    private int oldPosition = 0;
    private int currentIndex = 1;
    private int pageMargin = 0;
    private int leftMargin = 20;
    private int bottomMargin = 10;
    private static long time = 3000; // autoplay time delay
    private static boolean autoPlay = true;


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            play();
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            viewPager.setCurrentItem(++currentIndex);
        }
    };

    public AutoSlideViewPager(@NonNull Context context) {
        this(context,null);
    }

    public AutoSlideViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AutoSlideViewPager(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Dynamically add ViewPager and dots
     */
    private void init() {
        mContext = getContext();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setClipChildren(false); // don't clip the dots

        // set up ViewPager layout to match parent with 0 margin
        viewPager = new ViewPager(mContext);
        LayoutParams vparams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                650);
        viewPager.setLayoutParams(vparams);
        addView(viewPager);

        // set up layout for dots
        mLinearLayout = new LinearLayout(mContext);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        mLinearLayout.setGravity(Gravity.CENTER);
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(mLinearLayout,params);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        play();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancel();
    }

    /**
     * Initialize viewpager with dots
     */
    private void initViewPager() {
        if (mAdapter==null){
            return;
        }

        // set current image, number of cached image, and image margin
        viewPager.setCurrentItem(currentIndex);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setPageMargin(pageMargin);

        // add page change listener
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                // recycle through images
                if (position == 0){
                    currentIndex = mAdapter.getCount() - 2; // [x][1][2][3][4] --> [0][1][2][x][4]
                }else if (position == mAdapter.getCount() - 1){
                    currentIndex = 1; // [0][1][2][3][x] --> [0][x][2][3][4]
                }else {
                    currentIndex = position;
                }
                // recycle through dots
                mLinearLayout.getChildAt(oldPosition).setEnabled(false); // disable last dot
                mLinearLayout.getChildAt(currentIndex - 1).setEnabled(true); // currentIndex: 1,2,3
                oldPosition = currentIndex - 1; // currentIndex: 1,2,3
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // auto play unless user is dragging
                if(state == ViewPager.SCROLL_STATE_IDLE){
                    viewPager.setCurrentItem(currentIndex,false);
                    play();
                }else if(state == ViewPager.SCROLL_STATE_DRAGGING){
                    cancel();
                }
            }
        });

        // initialize dots
        setIndicatorDot();
        mLinearLayout.getChildAt(0).setEnabled(true);
    }

    /**
     * Start auto scrolling if autoPlay is true
     */
    public void play(){
        if (autoPlay){
            handler.postDelayed(runnable,time);
        }else {
            handler.removeCallbacks(runnable);
        }
    }

    /**
     * Cancel autoPlay
     */
    public void cancel(){
        handler.removeCallbacks(runnable);
    }

    /**
     * Set up dots
     */
    private void setIndicatorDot() {
        // add one dot for three images
        for (int i = 0; i < mAdapter.getCount() - 2; i++){
            // add gray dot
            View v = new View(mContext);
            v.setBackgroundResource(R.drawable.album_dot);
            v.setEnabled(false);

            // set up layout
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20,20);
            // set up margins between dots
            if (i != 0){
                params.leftMargin = leftMargin;
            }
            params.bottomMargin = bottomMargin;
            v.setLayoutParams(params);
            mLinearLayout.addView(v);
        }
    }

    public void setAdapter(T adpter){
        mAdapter = adpter ;
        viewPager.setAdapter(mAdapter);
        initViewPager();
    }

    public void setAutoPlay(boolean autoPlay) {
        AutoSlideViewPager.autoPlay = autoPlay;
        if (!autoPlay){
            cancel();
        }
    }
}
