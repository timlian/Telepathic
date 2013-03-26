package com.telepathic.finder.sdk.traffic.entity.kuaixin;

import java.util.Arrays;

public class KXStationLines {
	private String mName;
	private String mGpsNumber;
	private String[] mLines;
	private String[] mDirections;
	private int[] mIndices;

	public KXStationLines(String name, String gpsNumber, String[] lines) {
		mName = name;
		mGpsNumber = gpsNumber;
		mLines = Arrays.copyOf(lines, lines.length);
		mDirections = new String[lines.length];
		mIndices  = new int[lines.length];
	}

	public String getGpsNumber() {
		return mGpsNumber;
	}

	public String[] getLines() {
		return Arrays.copyOf(mLines, mLines.length);
	}

	public void setDirection(String lineNumber, String direction) {
		int index = find(lineNumber);
		if (index != -1) {
			mDirections[index] = direction;
		}
	}

	public void setStationIndex(String lineNumber, Integer index) {
		int pos = find(lineNumber);
		if (pos != -1) {
			mIndices[pos] = index;
		}
	}

	public int getStationIndex(String lineNumber) {
		int index = -1;
		int pos = find(lineNumber);
		if (pos != -1) {
			index = mIndices[pos];
		}
		return index;
	}

	public String getDirection(String lineNumber) {
		String direction = null;
		int index = find(lineNumber);
		if (index != -1) {
			direction = mDirections[index];
		}
		return direction;
	}

	public String getName() {
		return mName;
	}

	private int find(String lineNumber) {
		int index = -1;
		for (int idx = 0; idx < mLines.length; idx++) {
			if (mLines[idx].equals(lineNumber)) {
				index = idx;
			}
		}
		return index;
	}

}
