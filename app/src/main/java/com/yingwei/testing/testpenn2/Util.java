package com.yingwei.testing.testpenn2;

import android.content.Context;
import android.widget.Toast;

public class Util
{
	public static void showToast(Context context, final String msg )
	{
		Toast.makeText( context, msg, Toast.LENGTH_SHORT ).show();
	}
}
