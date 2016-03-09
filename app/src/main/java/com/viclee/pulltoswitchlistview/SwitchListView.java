package com.viclee.pulltoswitchlistview;
/**
 * Created by lixueyong on 2015/3/4.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class SwitchListView extends ListView implements OnScrollListener {

	private float mLastY = -1; // save event y
	private Scroller mScroller; // used for scroll back
	private OnScrollListener mScrollListener; // user's scroll listener

	// the interface to trigger refresh and load more.
	private SwitchListViewListener mListViewListener;

	// header view
	private SwitchListViewHeader mHeaderView;
	// header view content, use it to calculate the Header's height. And hide it when disable pull refresh
    public RelativeLayout mHeaderViewContent;
	private int mHeaderViewHeight; // header view's height
	private boolean mEnablePullRefresh = true;
	private boolean mPullRefreshing = false; // is refreashing.

	// footer view
	private SwitchListViewFooter mFooterView;
    public RelativeLayout mFooterViewContent;
    private int mFooterViewHeight; // header view's height
	private boolean mEnablePullLoad = true;
	private boolean mPullLoading = false;
	private boolean mIsFooterReady = false;

	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;

	// for mScroller, scroll back from header or footer.
	private int mScrollBack;
	private final static int SCROLLBACK_HEADER = 0;
	private final static int SCROLLBACK_FOOTER = 1;

	private static int mScrollDuration = 200; // scroll back duration
    private static int mScreenShotScrollDuration = 600;
	private final static float OFFSET_RADIO = 1.8f; // support iOS like pull feature.

    private boolean footerScrolled = false;

    private Bitmap maskBitmap = null;
    private View maskView;

	/**
	 * @param context
	 */
	public SwitchListView(Context context) {
		super(context);
		initWithContext(context);
	}

	public SwitchListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWithContext(context);
	}

	public SwitchListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWithContext(context);
	}

	private void initWithContext(Context context) {
		mScroller = new Scroller(context, new DecelerateInterpolator());
		// XListView need the scroll event, and it will dispatch the event to
		// user's listener (as a proxy).
		super.setOnScrollListener(this);

		// init header view
		mHeaderView = new SwitchListViewHeader(context);
		mHeaderViewContent = (RelativeLayout) mHeaderView
				.findViewById(R.id.switchlistview_header_content);
		addHeaderView(mHeaderView);

        // init header height
        mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mHeaderViewHeight = mHeaderViewContent.getHeight();
                        getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                    }
                });

		// init footer view
		mFooterView = new SwitchListViewFooter(context);

        mFooterViewContent = (RelativeLayout) mFooterView
                .findViewById(R.id.switchlistview_footer_content);
        // init footer height
        mFooterView.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mFooterViewHeight = mFooterViewContent.getHeight();
                        getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                    }
                });
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// make sure XListViewFooter is the last footer view, and only add once.
		super.setAdapter(adapter);
        if (mIsFooterReady == false) {
            mIsFooterReady = true;
            addFooterView(mFooterView);
        }
	}

	/**
	 * enable or disable pull down refresh feature.
	 * 
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			mHeaderViewContent.setVisibility(View.GONE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * enable or disable pull up load more feature.
	 * 
	 * @param enable
	 */
	public void setPullLoadEnable(boolean enable) {
		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
            mFooterViewContent.setVisibility(View.GONE);
        } else {
            mFooterViewContent.setVisibility(View.VISIBLE);
        }
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
		}
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
            resetFooterHeight();
		}
	}

	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnSwitchListViewScrollListener) {
			OnSwitchListViewScrollListener l = (OnSwitchListViewScrollListener) mScrollListener;
			l.onSwichListViewScrolling(this);
		}
	}

	private void updateHeaderHeight(float delta) {
		mHeaderView.setVisiableHeight((int) delta
				+ mHeaderView.getVisiableHeight());

        if (mEnablePullRefresh && !mPullRefreshing) { // 未处于刷新状态，更新箭头
            if (mHeaderView.getVisiableHeight() > mHeaderViewHeight/2) {
                mHeaderView.setState(SwitchListViewHeader.STATE_READY);
            } else {
                mHeaderView.setState(SwitchListViewHeader.STATE_NORMAL);
            }
        }

		setSelection(0); // scroll to top each time
	}

    private void updateFooterHeight(float delta) {
        mFooterView.setVisiableHeight((int) delta
                + mFooterView.getVisiableHeight());
        if (mEnablePullLoad && !mPullLoading) {
            if (mFooterView.getVisiableHeight() > mFooterViewHeight/2) {
                mFooterView.setState(SwitchListViewHeader.STATE_READY);
            } else {
                mFooterView.setState(SwitchListViewHeader.STATE_NORMAL);
            }
        }
    }

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height == 0) // not visible.
			return;

        mScrollBack = SCROLLBACK_HEADER;
        if (mPullRefreshing) {
            mScroller.startScroll(0, height, 0, mHeaderViewHeight/2 - height,
                    mScrollDuration);
        } else {
            mScroller.startScroll(0, height, 0, -height,
                    mScrollDuration);
        }
        invalidate();
	}

    private void resetFooterHeight() {
        int height = mFooterView.getVisiableHeight();
        if (height == 0) // not visible.
            return;

        mScrollBack = SCROLLBACK_FOOTER;
        if (mPullLoading) {
            mScroller.startScroll(0, height, 0, mFooterViewHeight/2 - height,
                    mScrollDuration);
        } else {
            mScroller.startScroll(0, height, 0, -height,
                    mScrollDuration);
        }
        invalidate();
    }

    public void setHeaderTitle(String headerTitle) {
        mHeaderView.setHeaderTitle(headerTitle);
    }

    public void setFooterTitle(String footerTitle) {
        mFooterView.setFooterTitle(footerTitle);
    }

    public void setHeaderBackground(Drawable drawable) {
        mHeaderViewContent.setBackgroundDrawable(drawable);
    }

    public void setFooterBackground(Drawable drawable) {
        mFooterViewContent.setBackgroundDrawable(drawable);
    }

    public void setHeaderBackground(int resId) {
        mHeaderViewContent.setBackgroundResource(resId);
    }

    public void setFooterBackground(int resId) {
        mFooterViewContent.setBackgroundResource(resId);
    }

    public void setScrollBackDuration(int time) {
        mScrollDuration = time;
    }

    public void setScreenShotScrollDuration(int duration){
        mScreenShotScrollDuration = duration;
    }

    public void setHeaderNoMore(boolean flag) {
        mHeaderView.mHeaderNoMore = flag;
    }
    public void setHeaderNoMoreText(String text) {
        mHeaderView.mNoMoreText.setText(text);
    }

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			if (getFirstVisiblePosition() == 0
					&& (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
				// the first item is showing, header has shown or pull down.
                if(mHeaderView.getVisiableHeight() <= mHeaderViewHeight) {
                    updateHeaderHeight(deltaY / OFFSET_RADIO);
                    invokeOnScrolling();
                }
			} else if (getLastVisiblePosition() == mTotalItemCount - 1
					&& (mFooterView.getVisiableHeight() > 0 || deltaY < 0)) {
				// last item, already pulled up or want to pull up.
                if(mFooterView.getVisiableHeight() <= mFooterViewHeight) {
                    updateFooterHeight(-deltaY / OFFSET_RADIO);
                }
			}
			break;
		default:
			mLastY = -1; // reset
			if (getFirstVisiblePosition() == 0) {
				// invoke refresh
				if (mEnablePullRefresh
						&& mHeaderView.getVisiableHeight() > mHeaderViewHeight/2) {
					mPullRefreshing = true;
					mHeaderView.setState(SwitchListViewHeader.STATE_REFRESHING);
					if (mListViewListener != null) {
						mListViewListener.onSwitchListViewRefresh();
					}
				}
				resetHeaderHeight();
			}
			if (getLastVisiblePosition() == mTotalItemCount - 1) {
				// invoke load more.
                if (mEnablePullLoad
						&& mFooterView.getVisiableHeight() > mFooterViewHeight/2) {
                    mPullLoading = true;
                    mFooterView.setState(SwitchListViewHeader.STATE_REFRESHING);
                    if (mListViewListener != null) {
                        mListViewListener.onSwitchListViewLoadMore();
                    }
				}
				resetFooterHeight();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());
			} else {
				mFooterView.setVisiableHeight(mScroller.getCurrY());
			}
			postInvalidate();
			invokeOnScrolling();
		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(footerScrolled == false) {
            footerScrolled = true;
        }

		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// send to user's listener
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}

	public void setXListViewListener(SwitchListViewListener l) {
		mListViewListener = l;
	}

	public interface OnSwitchListViewScrollListener extends OnScrollListener {
		public void onSwichListViewScrolling(View view);
	}

	/**
	 * implements this interface to get refresh/load more event.
	 */
	public interface SwitchListViewListener {
		public void onSwitchListViewRefresh();

		public void onSwitchListViewLoadMore();
	}


    public Bitmap getBitmap(Bitmap bitmap) {
        if(bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        for (int i = 0;i < 3;i++) {
            try {
                Bitmap result = Bitmap.createScaledBitmap(bitmap, width, height, false);
                return result;
            } catch (OutOfMemoryError e) {
                width/=2;
                height/=2;
            }
        }
        return null;
    }
    public void scrollScreenShot(boolean isDownPull, View view) {
        maskView = view;
        setDrawingCacheEnabled(true);
        buildDrawingCache();
//        maskBitmap = Bitmap.createBitmap(getDrawingCache());
        maskBitmap = getBitmap(getDrawingCache());
        if (maskBitmap == null) {
            return;
        }
        maskView.setVisibility(View.VISIBLE);
        maskView.setBackgroundDrawable(new BitmapDrawable(maskBitmap));
        setDrawingCacheEnabled(false);


        int maskHeight = maskView.getHeight();
        Animation translateAnimation;
        if(isDownPull) {
            translateAnimation = new TranslateAnimation(0, 0, 0, maskHeight);
        } else {
            translateAnimation = new TranslateAnimation(0, 0, 0, -maskHeight);
        }
        translateAnimation.setDuration(mScreenShotScrollDuration);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                maskView.setBackgroundDrawable(null);
                if(maskBitmap != null && !maskBitmap.isRecycled()) {
                    maskBitmap.recycle();
                    maskBitmap = null;
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        maskView.startAnimation(translateAnimation);
    }
}
