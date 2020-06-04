package com.mrikso.apkrepacker.autotranslator.common;

import android.content.Intent;
import android.os.Bundle;

public class ActivityUtil {

	public static String getParam(Intent intent, String key) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			return bundle.getString(key);
		}
		return null;
	}

	public static boolean getBoolParam(Intent intent, String key) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			return bundle.getBoolean(key, false);
		}
		return false;
	}

}
