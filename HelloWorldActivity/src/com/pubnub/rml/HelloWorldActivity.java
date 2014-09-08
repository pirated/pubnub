package com.pubnub.rml;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.pubnub.rml.R;

public class HelloWorldActivity extends Activity {


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		Button subscribe = (Button) findViewById(R.id.button1);

		subscribe.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent serviceIntent = new Intent(getApplicationContext(), PubnubService.class);
				startService(serviceIntent);
			}
		});

	}

}