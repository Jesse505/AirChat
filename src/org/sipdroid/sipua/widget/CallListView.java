package org.sipdroid.sipua.widget;

import android.content.Context;
import android.util.AttributeSet;

public class CallListView extends MoMoRefreshListView {

	public CallListView(Context context) {
		super(context);
		init();
	}

	public CallListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CallListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setStackFromBottom(true);
		setFastScrollEnabled(true);
	}
}
