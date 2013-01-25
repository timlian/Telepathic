/**
 * Copyright (C) 2012 Telepathic LTD. All Rights Reserved.
 * 
 * * Author: Tim Lian
 */
package com.telepathic.finder.app;

import java.util.ArrayList;

import com.telepathic.finder.util.Utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class MessageDispatcher {
	private static final String TAG = MessageDispatcher.class.getSimpleName();
	private ArrayList<IMessageHandler> mMessageHandlers = new ArrayList<IMessageHandler>();
	
	public interface IMessageHandler {
		
		public int what();
		
		public void handleMessage(Message msg);
	}
	
	public void add(IMessageHandler handler) {
		if (!mMessageHandlers.contains(handler)) {
			mMessageHandlers.add(handler);
		}
	}
	
	public void remove(IMessageHandler handler) {
		mMessageHandlers.remove(handler);
	}
	
	public Handler getMessageHandler(Looper looper) {
		return new MessageHandler(looper);
	}
	
	private class MessageHandler extends Handler {
		MessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Utils.debug(TAG, "handleMessage received: " + msg);
			for(IMessageHandler msgHandler : mMessageHandlers) {
				if (msgHandler.what() == msg.arg1) {
					msgHandler.handleMessage(msg);
				}
			}
		}
		
	}

}
