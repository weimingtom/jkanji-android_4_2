/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.cyberagent.android.gpuimage.sample.activity;

import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.jpegdecoder.JPEGDecoderActivity;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class ActivityMain extends Activity implements OnClickListener {
    private ActionBar actionBar;
	
	@Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gpuimage_activity_main);
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("图片滤镜");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.gpuimage;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        /*
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.memo;
			}

			@Override
			public void performAction(View view) {
				
			}
        });
        */
        
        
        
        findViewById(R.id.button_gallery).setOnClickListener(this);
        findViewById(R.id.button_jpegdecoder).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
        case R.id.button_gallery:
        	if (supportsOpenGLES2(this)) {
        		startActivity(ActivityGallery.class);
        	} else {
        		Toast.makeText(this, "不支持OpenGL ES 2.0，无法运行", Toast.LENGTH_SHORT).show();
        	}
            break;

        case R.id.button_jpegdecoder:
        	startActivity(JPEGDecoderActivity.class);
        	break;
        	
        default:
            break;
        }
    }

    private void startActivity(final Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }
    
    private boolean supportsOpenGLES2(final Context context) {
        final ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }
}
