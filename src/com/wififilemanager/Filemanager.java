/* Türkay Biliyor */
package com.wififilemanager;

import java.io.IOException;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Filemanager extends Activity {

	static public final String ACTION_STARTED = "com.wififilemanager.HTTPSERVER_STARTED";
	static public final String ACTION_STOPPED = "com.wififilemanager.HTTPSERVER_STOPPED";

	private Utils mUtils;

	private Button BtnStart;
	private TextView Statustxt, Desctxt;
	private HttpServer server;
	private int PORT = 1234;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mUtils = new Utils(this);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		Statustxt = (TextView) findViewById(R.id.txt_status);
		Desctxt = (TextView) findViewById(R.id.txt_desc);
		BtnStart = (Button) findViewById(R.id.btn_start);
		BtnStart.setText("Start");
		Statustxt.setText("");

		BtnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (BtnStart.getText().equals("Start")) {

					sendBroadcast(new Intent(ACTION_STARTED));

					Statustxt.setText("http://" + mUtils.getWifiApIpAddress()
							+ ":" + PORT);
					Desctxt.setText("Enter the adress in your web browser");

					try {
						server = new HttpServer(PORT);
					} catch (IOException e) {
						e.printStackTrace();
					}

					BtnStart.setText("Stop");
				} else {
					sendBroadcast(new Intent(ACTION_STOPPED));

					server.stop();
					Statustxt.setText("");
					Desctxt.setText("");
					BtnStart.setText("Start");
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}
