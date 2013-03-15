package com.telepathic.finder.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.os.Environment;

public class Logger {
	private static final String LOG_FILE = "finder.log";
	
	public static void logTrace(Thread thread, Throwable ex) {
		try {
			String logFile = Environment.getExternalStorageDirectory() + "/" + LOG_FILE;
			PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
			printWriter.println("Thread: " + thread);
			ex.printStackTrace(printWriter);
			printWriter.close();
		} catch (IOException e) {
			// ignore the io exception
		}
	}

}
