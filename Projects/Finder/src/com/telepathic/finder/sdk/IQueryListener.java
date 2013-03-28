package com.telepathic.finder.sdk;

import android.database.Cursor;

public interface IQueryListener {
	/**
	 * Called when the query operation finished.
	 * 
	 * @param data The result data.
	 */
	void done(Cursor data);
}
