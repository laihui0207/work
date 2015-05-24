package com.tuyou.tsd.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.tuyou.tsd.R;

public class ErrorActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.error_activity);

		Bundle bundle = getIntent().getBundleExtra("bundle");
		if (bundle != null) {
			String[] detail = bundle.getStringArray("missingComponents");
			StringBuilder builder = new StringBuilder();
			if (detail != null && detail.length > 0) {
				for (int i = 0; i < detail.length; i++) {
					builder.append(detail[i] + "\n");
				}
				TextView tv = (TextView) findViewById(R.id.missingDetailTextView);
				tv.setText(builder.toString());
			}
		}
	}

}
