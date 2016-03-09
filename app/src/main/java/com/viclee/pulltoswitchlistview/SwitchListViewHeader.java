package com.viclee.pulltoswitchlistview;
/**
 * Created by lixueyong on 2015/3/4.
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SwitchListViewHeader extends LinearLayout {
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;

    private int mState = STATE_NORMAL;

    public boolean mHeaderNoMore = true;

	private LinearLayout mContainer;
	private ProgressBar mProgressBar;
	private TextView mHintTextView;
    private ImageView mMoreBtn;
    private TextView mPreTitle;

    private RelativeLayout mLoadingStatusLayout;

    private RelativeLayout mNoMoreLayout;
    private View mHeaderShadow;
    public TextView mNoMoreText;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;

    private final int ROTATE_ANIM_DURATION = 200;

	public SwitchListViewHeader(Context context) {
		super(context);
		initView(context);
	}

	public SwitchListViewHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		// 初始情况，设置下拉刷新view高度为0
		LayoutParams lp = new LayoutParams(
				LayoutParams.FILL_PARENT, 0);
		mContainer = (LinearLayout) LayoutInflater.from(context).inflate(
				R.layout.switchlistview_header, null);
		addView(mContainer, lp);
		setGravity(Gravity.BOTTOM);

		mHintTextView = (TextView)findViewById(R.id.switchlistview_header_hint_textview);
		mProgressBar = (ProgressBar)findViewById(R.id.switchlistview_header_progressbar);
        mMoreBtn = (ImageView) findViewById(R.id.switchlistview_header_hint_more_btn);
        mPreTitle = (TextView)findViewById(R.id.switchlistview_header_hint_title);

        mLoadingStatusLayout = (RelativeLayout) findViewById(R.id.switchlistview_header_loading_status);

        mNoMoreLayout = (RelativeLayout) findViewById(R.id.switchlistview_header_no_more);
        mHeaderShadow = findViewById(R.id.switchlistview_header_shadow);
        mNoMoreText = (TextView) findViewById(R.id.switchlistview_header_no_more_text);

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);

        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
	}

	public void setState(int state) {
        if(mHeaderNoMore) {
            mNoMoreLayout.setVisibility(View.VISIBLE);
            mLoadingStatusLayout.setVisibility(View.GONE);
            mHeaderShadow.setVisibility(View.GONE);
            return;
        }
        mNoMoreLayout.setVisibility(View.GONE);
        mLoadingStatusLayout.setVisibility(View.VISIBLE);
        mHeaderShadow.setVisibility(View.VISIBLE);
        if (state == mState) return ;

        if (state == STATE_READY) {
            mMoreBtn.clearAnimation();
            mMoreBtn.startAnimation(mRotateUpAnim);
            mHintTextView.setText(R.string.switchlistview_header_release_to_pre_page);
        } else if (state == STATE_REFRESHING) {
            mHintTextView.setText(R.string.switchlistview_header_hint_loading);
            mMoreBtn.clearAnimation();
            mMoreBtn.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else if (state == STATE_NORMAL) {
            if (mState == STATE_READY) {
                mMoreBtn.startAnimation(mRotateDownAnim);
            } else if (mState == STATE_REFRESHING) {
                mMoreBtn.clearAnimation();
            }
            mHintTextView.setText(R.string.switchlistview_header_pull_to_pre_page);
            mProgressBar.setVisibility(View.INVISIBLE);
            mMoreBtn.setVisibility(View.VISIBLE);
        }

        mState = state;
	}
    public void setHeaderTitle(String title) {
        mPreTitle.setText(title);
    }
	
	public void setVisiableHeight(int height) {
		if (height < 0)
			height = 0;
		LayoutParams lp = (LayoutParams) mContainer
				.getLayoutParams();
		lp.height = height;
		mContainer.setLayoutParams(lp);
	}

	public int getVisiableHeight() {
		return mContainer.getHeight();
	}

}
