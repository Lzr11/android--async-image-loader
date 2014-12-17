package com.android.asyncimageloader;

import com.android.imageloader.core.AsyncImageloader;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private ImageView imv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		imv=(ImageView) findViewById(R.id.imv);
		AsyncImageloader.getInstance().displayImage(imv, "http://www.baidu.com/img/baidu_jgylogo3.gif?v=18630938.gif", R.drawable.ic_launcher);
	}
}
