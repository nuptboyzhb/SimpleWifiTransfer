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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Filemanager extends Activity {

	static public final String ACTION_STARTED = "com.wififilemanager.HTTPSERVER_STARTED";
	static public final String ACTION_STOPPED = "com.wififilemanager.HTTPSERVER_STOPPED";

	private ServerSocket mServerSocket;
	private Thread serverThread;

	private int TcpPort = 1234;
	private boolean runThread;

	private Button BtnStart;
	private TextView Statustxt, Desctxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		new Utils(this);

		Statustxt = (TextView) findViewById(R.id.txt_status);
		Desctxt = (TextView) findViewById(R.id.txt_desc);
		BtnStart = (Button) findViewById(R.id.btn_start);
		BtnStart.setText(R.string.start);
		BtnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (BtnStart.getText().equals("Start")) {
					try {
						newThread();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					stopServer();
				}
			}
		});

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_STARTED);
		filter.addAction(ACTION_STOPPED);
		registerReceiver(mFsActionsReceiver, filter);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		this.stopServer();
		unregisterReceiver(mFsActionsReceiver);
		super.onDestroy();
	}

	private void newThread() throws IOException {
		runThread = true;
		mServerSocket = new ServerSocket(TcpPort);

		serverThread = new Thread(new Runnable() {
			public void run() {
				try {
					while (runThread) {
						sendBroadcast(new Intent(ACTION_STARTED));
						new HTTPSession(mServerSocket.accept());
					}
				} catch (IOException ioe) {
					stopServer();
				}
			}
		});

		serverThread.setDaemon(true);
		serverThread.start();
	}

	public void stopServer() {
		sendBroadcast(new Intent(ACTION_STOPPED));

		this.runThread = false;

		try {
			if (mServerSocket != null)
				mServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This receiver will check HTTPServer.ACTION* messages and will update the
	 * button, running_state, if the server is running and will also display at
	 * what url the server is running.
	 */
	BroadcastReceiver mFsActionsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_STARTED)) {
				Statustxt.setText(Utils.getAdressSum());
				Desctxt.setText(R.string.enterip);
				BtnStart.setText(R.string.stop);

				// Fill in the HTTP server address
				InetAddress address = null;

				try {
					address = InetAddress.getByName(Utils.getIPAddress(true));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}

				if (address == null) {
					Statustxt.setText(R.string.urlerror);
					return;
				}

			} else if (intent.getAction().equals(ACTION_STOPPED)) {
				BtnStart.setText(R.string.start);
				Statustxt.setText("");
				Desctxt.setText("");
			}
		}
	};
}
