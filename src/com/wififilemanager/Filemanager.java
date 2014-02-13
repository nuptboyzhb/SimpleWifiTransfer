/*
 * Copyright (C) 2014 SimpleWifiTransfer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package com.wififilemanager;

import java.io.IOException;
import java.net.ServerSocket;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Filemanager extends Activity {

	static public final String ACTION_STARTED = "com.wififilemanager.HTTPSERVER_STARTED";
	static public final String ACTION_STOPPED = "com.wififilemanager.HTTPSERVER_STOPPED";

	private int TcpPort = 1234;
	private boolean runThread;
	private ServerSocket mServerSocket;

	private Button BtnStart;
	private TextView Statustxt, Desctxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		new Utils(this);

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
					startServer();
					BtnStart.setText("Stop");
				} else {
					stopServer();
					BtnStart.setText("Start");
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		this.stop();
	}

	private void startServer() {
		sendBroadcast(new Intent(ACTION_STARTED));

		Statustxt.setText(Utils.getAdressSum());
		Desctxt.setText("Enter the adress in your web browser");

		try {
			newThread();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void newThread() throws IOException {
		runThread = true;
		mServerSocket = new ServerSocket(TcpPort);

		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					while (runThread)
						new HTTPSession(mServerSocket.accept());
				} catch (IOException ioe) {
					Log.i("LOG_pawn", "http");
				}
			}
		});

		t.setDaemon(true);
		t.start();
	}

	private void stopServer() {
		sendBroadcast(new Intent(ACTION_STOPPED));

		this.stop();
		Statustxt.setText("");
		Desctxt.setText("");
	}

	public void stop() {
		this.runThread = false;

		try {
			if (mServerSocket != null)
				mServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
