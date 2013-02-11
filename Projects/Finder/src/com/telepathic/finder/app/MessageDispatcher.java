/**
 * Copyright (C) 2013 Telepathic LTD. All Rights Reserved.
 * 
 * * Author: Tim Lian
 */
package com.telepathic.finder.app;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

class MessageDispatcher {
	private ArrayList<IMessageHandler> mMessageHandlers = new ArrayList<IMessageHandler>();
	
	interface IMessageHandler {
		
		public int what();
		
		public void handleMessage(Message msg);
	}
	
	void add(IMessageHandler handler) {
		if (!mMessageHandlers.contains(handler)) {
			mMessageHandlers.add(handler);
		}
	}
	
	void remove(IMessageHandler handler) {
		mMessageHandlers.remove(handler);
	}
	
	Handler getMessageHandler(Looper looper) {
		return new MessageHandler(looper);
	}
	
	private class MessageHandler extends Handler {
		MessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			for(IMessageHandler msgHandler : mMessageHandlers) {
				if (msgHandler.what() == msg.arg1) {
					msgHandler.handleMessage(msg);
				}
			}
		}
	}

}
