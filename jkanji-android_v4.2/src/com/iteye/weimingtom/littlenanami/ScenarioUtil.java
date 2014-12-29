package com.iteye.weimingtom.littlenanami;

import android.content.Context;
import android.util.Log;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScenarioUtil {
	private final static boolean D = false;
	private final static String TAG = "ScenarioUtil";
	
	public static int getScenario(Context context, String filename, int widgetid, int defaultScenario) {
		FileInputStream fis = null;
		DataInputStream dis = null;
		try {
			fis = context.openFileInput(filename);
			dis = new DataInputStream(fis);
			String line = null;
			do {
				line = dis.readLine();
				if (line != null) {
					String[] strs = line.split(",");
					if (strs != null && strs.length >= 2) {
						int wid = Integer.valueOf(strs[0]).intValue();
						int sce = Integer.valueOf(strs[1]).intValue();
						if (D) {
							Log.d(TAG, "getScenario" + wid + "=>" + sce);
						}
						if (wid == widgetid) {
							if (D) {
								Log.d(TAG, "getScenario return " + wid + "=>" + sce);
							}
							return sce;
						}
					}
				}
			} while (line != null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return defaultScenario;
	}

	public static void putScenario(Context context, String filename, int widgetid, int scenario) {
		HashMap<Integer, Integer> records = new HashMap<Integer, Integer>();
		FileInputStream fis = null;
		DataInputStream dis = null;
		try {
			fis = context.openFileInput(filename);
			dis = new DataInputStream(fis);
			String line = null;
			do {
				line = dis.readLine();
				if (line != null) {
					String[] strs = line.split(",");
					if (strs != null && strs.length >= 2) {
						int wid = Integer.valueOf(strs[0]).intValue();
						int sce = Integer.valueOf(strs[1]).intValue();
						records.put(wid, sce);
					}
				}
			} while (line != null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		records.put(widgetid, scenario);
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			for (Map.Entry<Integer, Integer> record : records.entrySet()) {
				fos.write((record.getKey() + "," + record.getValue() + "\n").getBytes());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void clearAllScenario(Context context, String filename) {
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			fos.write("".getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
