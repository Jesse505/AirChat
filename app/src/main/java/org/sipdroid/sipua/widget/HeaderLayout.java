package org.sipdroid.sipua.widget;

import org.sipdroid.sipua.R;
import org.sipdroid.sipua.widget.HeaderSpinner.onSpinnerClickListener;
import org.sipdroid.sipua.widget.SwitcherButton.onSwitcherButtonClickListener;

import android.R.integer;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


public class HeaderLayout extends RelativeLayout {
	

    private LayoutInflater mInflater;
    private View mHeader;
    private Button mbtn_back;
    private onLeftImageButtonClickListener mLeftImageButtonClickListener;
    
    private Button mbtn_right;

    // 标题
    private LinearLayout mLayoutTitle;
    private ScrollingTextView mStvTitle;
    private HandyTextView mHtvSubTitle;


    // 右边文本
    private HandyTextView mHtvRightText;

    // 右边按钮
    private LinearLayout mLayoutRightImageButtonLayout;
    private ImageButton mIbRightImageButton;
    private onRightImageButtonClickListener mRightImageButtonClickListener;

    private HeaderSpinner mHsSpinner;
    private LinearLayout mLayoutMiddleImageButtonLayout;
    private ImageButton mIbMiddleImageButton;
    private ImageView mIvMiddleLine;
    private onMiddleImageButtonClickListener mMiddleImageButtonClickListener;


    public HeaderLayout(Context context) {
        super(context);
        init(context);
    }

    public HeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        mInflater = LayoutInflater.from(context);
        mHeader = mInflater.inflate(R.layout.common_headerbar, null);
        addView(mHeader);
        initViews();

    }

    public void initViews() {
        mbtn_back = (Button) findViewByHeaderId(R.id.btn_back);
        mbtn_right = (Button) findViewByHeaderId(R.id.btn_right);;
        mLayoutTitle = (LinearLayout) findViewByHeaderId(R.id.header_layout_title);
        mStvTitle = (ScrollingTextView) findViewByHeaderId(R.id.header_stv_title);
        mHtvSubTitle = (HandyTextView) findViewByHeaderId(R.id.header_htv_subtitle);

        mHsSpinner = (HeaderSpinner) findViewByHeaderId(R.id.header_hs_spinner);
        mLayoutMiddleImageButtonLayout = (LinearLayout) findViewByHeaderId(R.id.header_layout_middle_imagebuttonlayout);
        mIbMiddleImageButton = (ImageButton) findViewByHeaderId(R.id.header_ib_middle_imagebutton);
        mIvMiddleLine = (ImageView) findViewByHeaderId(R.id.header_iv_middle_line);

    }

    public View findViewByHeaderId(int id) {
        return mHeader.findViewById(id);
    }

    public void init(HeaderStyle style) {
        switch (style) {
            case DEFAULT_TITLE:
                defaultTitle();
                break;

            case TITLE_RIGHT_TEXT:
                titleRightText();
                break;

            case TITLE_RIGHT_IMAGEBUTTON:
                titleRightImageButton();
                break;

            case TITLE_NEARBY_PEOPLE:
                titleNearBy(true);
                break;

            case TITLE_NEARBY_GROUP:
                titleNearBy(false);
                break;

            case TITLE_CHAT:
                titleChat();
                break;
        }
    }
    
    public void setRightBtnVisibility(boolean isVisible){
    	if (isVisible) {
			mbtn_right.setVisibility(View.VISIBLE);
		}else {
			mbtn_right.setVisibility(View.GONE);
		}
    }

    /**
     * 默认只有标题
     */
    private void defaultTitle() {
        mLayoutTitle.setVisibility(View.VISIBLE);
    }

    /**
     * 添加默认标题内容
     * 
     * @param title
     * @param subTitle
     */
    public void setDefaultTitle(CharSequence title, CharSequence subTitle) {
        if (title != null) {
            mStvTitle.setText(title);
        }
        else {
            mStvTitle.setVisibility(View.GONE);
        }
        if (subTitle != null) {
            mHtvSubTitle.setText(subTitle);
        }
        else {
            mHtvSubTitle.setVisibility(View.GONE);
        }
    }

    /**
     * 标题以及右边有文本内容
     */
    private void titleRightText() {
        mLayoutTitle.setVisibility(View.VISIBLE);
        View mRightText = mInflater.inflate(R.layout.include_header_righttext, null);
        mHtvRightText = (HandyTextView) mRightText.findViewById(R.id.header_htv_righttext);
    }

    /**
     * 添加标题以及右边文本内容
     * 
     * @param title
     * @param subTitle
     * @param rightText
     */
    public void
            setTitleRightText(CharSequence title, CharSequence subTitle, CharSequence rightText) {
        setDefaultTitle(title, subTitle);
        if (mHtvRightText != null && rightText != null) {
            mHtvRightText.setText(rightText);
        }
    }

    /**
     * 标题以及右边图片按钮
     */
    private void titleRightImageButton() {
        mLayoutTitle.setVisibility(View.VISIBLE);
        View mRightImageButton = mInflater.inflate(R.layout.include_header_rightimagebutton, null);
        mLayoutRightImageButtonLayout = (LinearLayout) mRightImageButton
                .findViewById(R.id.header_layout_right_imagebuttonlayout);
        mIbRightImageButton = (ImageButton) mRightImageButton
                .findViewById(R.id.header_ib_right_imagebutton);
        mLayoutRightImageButtonLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mRightImageButtonClickListener != null) {
                    mRightImageButtonClickListener.onClick();
                }
            }
        });
    }

    public void setTitleRightImageButton(CharSequence title, CharSequence subTitle, int id,
            onRightImageButtonClickListener listener) {
        setDefaultTitle(title, subTitle);
        if (mIbRightImageButton != null && id > 0) {
            mIbRightImageButton.setImageResource(id);
            setOnRightImageButtonClickListener(listener);
        }
    }

    // "附近"标题
    private void titleNearBy(boolean isPeople) {
        if (isPeople) {
            mLayoutTitle.setVisibility(View.GONE);
            mHsSpinner.setVisibility(View.VISIBLE);
            mLayoutMiddleImageButtonLayout.setVisibility(View.GONE);
        }
        else {
            mLayoutTitle.setVisibility(View.VISIBLE);
            mHsSpinner.setVisibility(View.GONE);
            mLayoutMiddleImageButtonLayout.setVisibility(View.VISIBLE);
        }
    }

    public HeaderSpinner setTitleNearBy(CharSequence spinnerText,
            onSpinnerClickListener spinnerClickListener, CharSequence title, int middleImageId,
            onMiddleImageButtonClickListener middleImageButtonClickListener,
            CharSequence switcherLeftText, CharSequence switcherRightText,
            onSwitcherButtonClickListener switcherButtonClickListener) {

        mHsSpinner.setText(spinnerText);
        mHsSpinner.setOnSpinnerClickListener(spinnerClickListener);
        setDefaultTitle(title, null);
        if (middleImageId > 0) {
            mIbMiddleImageButton.setImageResource(middleImageId);
        }
        mMiddleImageButtonClickListener = middleImageButtonClickListener;
        mLayoutMiddleImageButtonLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMiddleImageButtonClickListener != null) {
                    mMiddleImageButtonClickListener.onClick();
                }
            }
        });
        return mHsSpinner;
    }

    private void titleChat() {
        mLayoutTitle.setVisibility(View.VISIBLE);
        mIvMiddleLine.setVisibility(View.GONE);
        View mRightImageButton = mInflater.inflate(R.layout.include_header_rightimagebutton, null);
         mLayoutRightImageButtonLayout = (LinearLayout) mRightImageButton
                .findViewById(R.id.header_layout_right_imagebuttonlayout);
        mIbRightImageButton = (ImageButton) mRightImageButton
                .findViewById(R.id.header_ib_right_imagebutton);
        mLayoutRightImageButtonLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mRightImageButtonClickListener != null) {
                    mRightImageButtonClickListener.onClick();
                }
            }
        });
        mLayoutMiddleImageButtonLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMiddleImageButtonClickListener != null) {
                    mMiddleImageButtonClickListener.onClick();
                }
            }
        });
        mbtn_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mLeftImageButtonClickListener != null ) {
					mLeftImageButtonClickListener.onClick();
				}
				
			}
		});
        mbtn_right.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mRightImageButtonClickListener != null) {
					mRightImageButtonClickListener.onClick();
				}
				
			}
		});
    }

    public void setTitleChat( CharSequence title,
            CharSequence subTitle, int rightImageId,
            onRightImageButtonClickListener rightImageButtonClickListener) {
        setDefaultTitle(title, subTitle);
        mIbRightImageButton.setImageResource(rightImageId);
        mRightImageButtonClickListener = rightImageButtonClickListener;

    }

    public enum HeaderStyle {
        DEFAULT_TITLE, TITLE_RIGHT_TEXT, TITLE_RIGHT_IMAGEBUTTON, TITLE_NEARBY_PEOPLE, TITLE_NEARBY_GROUP, TITLE_CHAT;
    }

    public enum SearchState {
        INPUT, SEARCH;
    }


    public void setOnMiddleImageButtonClickListener(onMiddleImageButtonClickListener listener) {
        mMiddleImageButtonClickListener = listener;
    }

    public void setOnRightImageButtonClickListener(onRightImageButtonClickListener listener) {
        mRightImageButtonClickListener = listener;
    }

    public void setOnLeftIamgeButtonClickListener(onLeftImageButtonClickListener listener){
    	mLeftImageButtonClickListener = listener;
    }
    
    public interface onLeftImageButtonClickListener{
    	void onClick();
    }
    
    public interface onMiddleImageButtonClickListener {
        void onClick();
    }

    public interface onRightImageButtonClickListener {
        void onClick();
    }
}
