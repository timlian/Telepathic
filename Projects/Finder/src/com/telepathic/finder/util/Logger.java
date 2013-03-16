package com.telepathic.finder.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import android.os.Environment;

public class Logger {
	private static final String LOG_FILE = "finder.log";
	
	public static void logTrace(Thread thread, Throwable ex) {
		try {
			String logFile = Environment.getExternalStorageDirectory() + "/" + LOG_FILE;
			PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
			printWriter.println("Exception Entry: " + Utils.formatDate(new Date()));
			printWriter.println("Thread: " + thread);
			ex.printStackTrace(printWriter);
			printWriter.close();
		} catch (IOException e) {
			// ignore the io exception
		}
	}
	
	public static void logTrace(String errorText) {
		try {
			String logFile = Environment.getExternalStorageDirectory() + "/" + LOG_FILE;
			PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
			printWriter.println("Error Entry: " + Utils.formatDate(new Date()));
			printWriter.println(errorText);
			printWriter.close();
		} catch (IOException e) {
			// ignore the io exception
		}
	}

}
