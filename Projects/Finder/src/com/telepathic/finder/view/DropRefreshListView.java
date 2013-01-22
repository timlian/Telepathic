package com.telepathic.finder.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.telepathic.finder.R;

public class DropRefreshListView extends ListView implements OnScrollListener {

    private static final String LOG_TAG = "DropRefreshListView";

    private final static int RELEASE_TO_REFRESH = 0;
    private final static int PULL_TO_REFRESH = 1;
    private final static int REFRESHING = 2;
    private final static int DONE = 3;
    private final static int LOADING = 4;

    // The ratio of actual padding distance and surface offset distance
    private final static int RATIO = 3;

    // Make sure the startY record only once in a whole touch event
    private boolean isRecored;

    private LinearLayout mHeaderView;
    private ImageView mIvArrow;
    private ProgressBar mProgressBar;
    private TextView mTvTips;

    private int mHeadContentWidth;
    private int mHeadContentHeight;
    private int startY;
    private int mFirstItemIndex;

    private RotateAnimation mAnimation;
    private RotateAnimation mReverseAnimation;

    private int mState;
    private boolean isRefreshable;
    private boolean isBack;

    private OnRefreshListener mRefreshListener;

    public interface OnRefreshListener {
        public void onRefresh();
    }

    public DropRefreshListView(Context context) {
        super(context);
        init(context);
    }
    public DropRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mHeaderView = (LinearLayout)inflater.inflate(R.layout.list_header, null);

        mIvArrow = (ImageView)mHeaderView.findViewById(R.id.arrow);
        mProgressBar = (ProgressBar)mHeaderView.findViewById(R.id.progressbar);
        mTvTips = (TextView)mHeaderView.findViewById(R.id.tips_text);

        measureView(mHeaderView);
        mHeadContentHeight = mHeaderView.getMeasuredHeight();
        mHeadContentWidth = mHeaderView.getMeasuredWidth();

        mHeaderView.setPadding(0, -1 * mHeadContentHeight, 0, 0);
        mHeaderView.invalidate();

        addHeaderView(mHeaderView, null, false);
        setOnScrollListener(this);

        mAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setDuration(250);
        mAnimation.setFillAfter(true);

        mReverseAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseAnimation.setInterpolator(new LinearInterpolator());
        mReverseAnimation.setDuration(200);
        mReverseAnimation.setFillAfter(true);

        mState = DONE;
        isRefreshable = false;
    }
    @Override
    public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2, int arg3) {
        mFirstItemIndex = firstVisiableItem;
    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0,
                0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isRefreshable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mFirstItemIndex == 0 && !isRecored) {
                        isRecored = true;
                        startY = (int) event.getY();
                        Log.v(LOG_TAG, "Record current position when down");
                    }
                    break;

                case MotionEvent.ACTION_UP:

                    if (mState != REFRESHING && mState != LOADING) {
                        if (mState == DONE) {
                            // Do nothing
                        }
                        if (mState == PULL_TO_REFRESH) {
                            mState = DONE;
                            changeHeaderViewByState();

                            Log.v(LOG_TAG, "From drop refresh state to done state");
                        }
                        if (mState == RELEASE_TO_REFRESH) {
                            mState = REFRESHING;
                            changeHeaderViewByState();
                            onRefresh();

                            Log.v(LOG_TAG, "From release refresh state to refreshing state");
                        }
                    }

                    isRecored = false;
                    isBack = false;

                    break;

                case MotionEvent.ACTION_MOVE:
                    int tempY = (int) event.getY();

                    if (!isRecored && mFirstItemIndex == 0) {
                        Log.v(LOG_TAG, "Record the position when move");
                        isRecored = true;
                        startY = tempY;
                    }

                    if (mState != REFRESHING && isRecored && mState != LOADING) {

                        // Make sure the current position is on header
                        // during setting the padding to avoid the list will scroll when push up.

                        // Enable release to refresh
                        if (mState == RELEASE_TO_REFRESH) {

                            setSelection(0);

                            // Push up, the header is covered but not fully out of screen
                            if (((tempY - startY) / RATIO < mHeadContentHeight)
                                    && (tempY - startY) > 0) {
                                mState = PULL_TO_REFRESH;
                                changeHeaderViewByState();

                                Log.v(LOG_TAG, "From release refresh state to pull refresh state");
                            }
                            // Push to top
                            else if (tempY - startY <= 0) {
                                mState = DONE;
                                changeHeaderViewByState();

                                Log.v(LOG_TAG, "From release refresh state to done state");
                            }
                            // Pull down or the header is not push to the top
                            else {
                                // Do nothing, just update the value of paddingTop
                            }
                        }
                        // Not display release to refresh, in done or pull refresh state
                        if (mState == PULL_TO_REFRESH) {

                            setSelection(0);

                            // Pull down to enter release refresh state
                            if ((tempY - startY) / RATIO >= mHeadContentHeight) {
                                mState = RELEASE_TO_REFRESH;
                                isBack = true;
                                changeHeaderViewByState();

                                Log.v(LOG_TAG, "From done or pull refresh to release refresh");
                            }
                            // Push to top
                            else if (tempY - startY <= 0) {
                                mState = DONE;
                                changeHeaderViewByState();

                                Log.v(LOG_TAG, "From done or pull refresh to done");
                            }
                        }

                        // done state
                        if (mState == DONE) {
                            if (tempY - startY > 0) {
                                mState = PULL_TO_REFRESH;
                                changeHeaderViewByState();
                            }
                        }

                        // Update the size of headView
                        if (mState == PULL_TO_REFRESH) {
                            mHeaderView.setPadding(0, -1 * mHeadContentHeight
                                    + (tempY - startY) / RATIO, 0, 0);
                        }

                        // Update the paddingTop of headView
                        if (mState == RELEASE_TO_REFRESH) {
                            mHeaderView.setPadding(0, (tempY - startY) / RATIO
                                    - mHeadContentHeight, 0, 0);
                        }
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    // Invoke the method to update surface when the state changed
    private void changeHeaderViewByState() {
        switch (mState) {
            case RELEASE_TO_REFRESH:
                mIvArrow.setVisibility(VISIBLE);
                mProgressBar.setVisibility(GONE);

                mIvArrow.clearAnimation();
                mIvArrow.startAnimation(mAnimation);

                mTvTips.setText(R.string.release_to_refresh);

                Log.v(LOG_TAG, "Release to refresh");
                break;
            case PULL_TO_REFRESH:
                mIvArrow.setVisibility(VISIBLE);
                mProgressBar.setVisibility(GONE);
                mIvArrow.clearAnimation();

                if(isBack) {
                    isBack = false;
                    mIvArrow.clearAnimation();
                    mIvArrow.startAnimation(mReverseAnimation);

                    mTvTips.setText(R.string.pull_to_refresh);
                } else {
                    mTvTips.setText(R.string.pull_to_refresh);
                }
                Log.v(LOG_TAG, "Pull to refresh");
                break;
            case REFRESHING:
                mHeaderView.setPadding(0, 0, 0, 0);

                mProgressBar.setVisibility(VISIBLE);
                mIvArrow.clearAnimation();
                mIvArrow.setVisibility(GONE);
                mTvTips.setText(R.string.refreshing);

                Log.v(LOG_TAG, "Refreshing");
                break;
            case DONE:
                mHeaderView.setPadding(0, -1 * mHeadContentHeight, 0, 0);
                mProgressBar.setVisibility(GONE);
                mIvArrow.setVisibility(VISIBLE);
                mIvArrow.clearAnimation();
                mIvArrow.setImageResource(R.drawable.arrow_down);
                mTvTips.setText(R.string.pull_to_refresh);

                Log.v(LOG_TAG, "Done");
                break;
        }
    }

    private void onRefresh() {
        if (mRefreshListener != null) {
            mRefreshListener.onRefresh();
        }
    }

    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.mRefreshListener = refreshListener;
        isRefreshable = true;
    }

    public void onRefreshComplete() {
        mState = DONE;
        changeHeaderViewByState();
    }

    public void setAdapter(BaseAdapter adapter) {
        super.setAdapter(adapter);
    }
}
