package com.viclee.pulltoswitchlistview;
/**
 * Created by lixueyong on 2015/3/4.
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SwitchListViewFooter extends LinearLayout {
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_LOADING = 2;

    private int mState = STATE_NORMAL;

	private Context mContext;

    private LinearLayout mContainer;

	private View mContentView;
    private ImageView mMoreBtn;
	private TextView mHintTextView;
	private TextView mNextTitle;
    private ProgressBar mProgressBar;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;

    private final int ROTATE_ANIM_DURATION = 200;

	public SwitchListViewFooter(Context context) {
		super(context);
		initView(context);
	}
	
	public SwitchListViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

    private void initView(Context context) {
        mContext = context;
        mContainer = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.switchlistview_footer, null);
        addView(mContainer);
        mContainer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 0));

        mContentView = findViewById(R.id.switchlistview_footer_content);
        mHintTextView = (TextView)findViewById(R.id.switchlistview_footer_hint_textview);
        mNextTitle = (TextView)findViewById(R.id.switchlistview_footer_hint_title);
        mMoreBtn = (ImageView) findViewById(R.id.switchlistview_footer_hint_more_btn);
        mProgressBar = (ProgressBar) findViewById(R.id.switchlistview_footer_progressbar);

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
        if (state == mState) return ;

        if (state == STATE_READY) {
            mMoreBtn.clearAnimation();
            mMoreBtn.startAnimation(mRotateUpAnim);
            mHintTextView.setText(R.string.switchlistview_footer_release_to_next_page);
        } else if (state == STATE_LOADING) {
            loading();
        } else if (state == STATE_NORMAL) {
            if (mState == STATE_READY) {
                mMoreBtn.startAnimation(mRotateDownAnim);
            } else if (mState == STATE_LOADING) {
                mMoreBtn.clearAnimation();
            }
            mHintTextView.setText(R.string.switchlistview_footer_pull_to_next_page);
            mProgressBar.setVisibility(View.INVISIBLE);
            mMoreBtn.setVisibility(View.VISIBLE);
        }
        mState = state;
	}

	/**
	 * loading status 
	 */
	public void loading() {
        mHintTextView.setText(R.string.switchlistview_header_hint_loading);
        mMoreBtn.clearAnimation();
        mMoreBtn.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
	}
	
	/**
	 * hide footer when disable pull load more
	 */
	public void hide() {
		LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();
		lp.height = 0;
		mContentView.setLayoutParams(lp);
	}
	
	/**
	 * show footer
	 */
	public void show() {
		LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		mContentView.setLayoutParams(lp);
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

    public void setFooterTitle(String title) {
        mNextTitle.setText(title);
    }
}
