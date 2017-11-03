package doext.implement.activity;

import org.json.JSONException;
import org.json.JSONObject;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.easefun.polyvsdk.ijk.IjkVideoView;
import com.easefun.polyvsdk.ijk.IjkVideoView.ErrorReason;
import com.easefun.polyvsdk.ijk.OnPreparedListener;

import core.helper.DoResourcesHelper;
import doext.implement.DoPolyvConstant;
import doext.implement.custom.MediaController;
import doext.implement.inteface.IDojkVideo;

public class IjkVideoActicity extends Activity implements IDojkVideo {
	private IjkVideoView videoView = null;
	private MediaController mediaController = null;
	private ProgressBar progressBar = null;
	private int w = 0, h = 0;
	private RelativeLayout rl = null;
	private int stopPosition = 0;
	private int bitRate = 0;
	private String path;
	private String vid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DoPolyvConstant.ijkVideo = this;
		int do_polyv_video_small = DoResourcesHelper.getIdentifierByStr("do_polyv_video_small", "layout", DoPolyvConstant.TYPEID);
		setContentView(do_polyv_video_small);
		// ------------------------------------------------------
		Bundle e = getIntent().getExtras();
		if (e != null) {
			path = e.getString("path");
			vid = e.getString("vid");
			stopPosition = e.getInt("position");
			bitRate = e.getInt("bitRate");
		}

		Point point = new Point();
		WindowManager wm = this.getWindowManager();
		wm.getDefaultDisplay().getSize(point);
		w = point.x;
		h = point.y;
		int rl_id = DoResourcesHelper.getIdentifierByStr("rl", "id", DoPolyvConstant.TYPEID);
		rl = (RelativeLayout) findViewById(rl_id);
		int videoview_id = DoResourcesHelper.getIdentifierByStr("videoview", "id", DoPolyvConstant.TYPEID);
		videoView = (IjkVideoView) findViewById(videoview_id);
		int loadingprogress_id = DoResourcesHelper.getIdentifierByStr("loadingprogress", "id", DoPolyvConstant.TYPEID);
		progressBar = (ProgressBar) findViewById(loadingprogress_id);
		// 在缓冲时出现的loading
		videoView.setMediaBufferingIndicator(progressBar);
		videoView.setVideoLayout(IjkVideoView.VIDEO_LAYOUT_SCALE);
//		videoView.switchLevel(bitRate);
		videoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(IMediaPlayer mp) {
				videoView.setVideoLayout(IjkVideoView.VIDEO_LAYOUT_SCALE);
				if (stopPosition > 0) {
					videoView.seekTo(stopPosition);
				}
			}
		});

		videoView.setOnVideoStatusListener(new IjkVideoView.OnVideoStatusListener() {

			@Override
			public void onStatus(int status) {

			}
		});

		videoView.setOnVideoPlayErrorLisener(new IjkVideoView.OnVideoPlayErrorLisener() {

			@Override
			public boolean onVideoPlayError(ErrorReason errorReason) {
				return false;
			}
		});

		mediaController = new MediaController(this, false);
		mediaController.setIjkVideoView(videoView);
		mediaController.setAnchorView(videoView);
		videoView.setMediaController(mediaController);
		if (path != null && path.length() > 0) {
			progressBar.setVisibility(View.GONE);
			videoView.setVideoPath(path);
		} else {
			if (bitRate == 0) {
				videoView.setVid(vid);
			} else {
				videoView.setVid(vid, bitRate);
			}
		}

		// 设置切屏事件
		mediaController.setOnBoardChangeListener(new MediaController.OnBoardChangeListener() {

			@Override
			public void onPortrait() {
				changeToLandscape();
			}

			@Override
			public void onLandscape() {
				changeToPortrait();
			}
		});

		// 设置视频尺寸 ，在横屏下效果较明显
		mediaController.setOnVideoChangeListener(new MediaController.OnVideoChangeListener() {

			@Override
			public void onVideoChange(final int layout) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						videoView.setVideoLayout(layout);
						switch (layout) {
						case IjkVideoView.VIDEO_LAYOUT_ORIGIN:
							Toast.makeText(IjkVideoActicity.this, "VIDEO_LAYOUT_ORIGIN", Toast.LENGTH_SHORT).show();
							break;
						case IjkVideoView.VIDEO_LAYOUT_SCALE:
							Toast.makeText(IjkVideoActicity.this, "VIDEO_LAYOUT_SCALE", Toast.LENGTH_SHORT).show();
							break;
						case IjkVideoView.VIDEO_LAYOUT_STRETCH:
							Toast.makeText(IjkVideoActicity.this, "VIDEO_LAYOUT_STRETCH", Toast.LENGTH_SHORT).show();
							break;
						case IjkVideoView.VIDEO_LAYOUT_ZOOM:
							Toast.makeText(IjkVideoActicity.this, "VIDEO_LAYOUT_ZOOM", Toast.LENGTH_SHORT).show();
							break;
						}
					}
				});
			}
		});
	}

	/**
	 * 切换到横屏
	 */
	public void changeToLandscape() {
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(h, w);
		rl.setLayoutParams(p);
		rl.setGravity(Gravity.CENTER);
		stopPosition = videoView.getCurrentPosition();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	/**
	 * 切换到竖屏
	 */
	public void changeToPortrait() {
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w, h);
		rl.setLayoutParams(p);
		stopPosition = videoView.getCurrentPosition();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	// 配置文件设置congfigchange 切屏调用一次该方法，hide()之后再次show才会出现在正确位置
	@Override
	public void onConfigurationChanged(Configuration arg0) {
		super.onConfigurationChanged(arg0);
		videoView.setVideoLayout(IjkVideoView.VIDEO_LAYOUT_SCALE);
		mediaController.hide();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean value = mediaController.dispatchKeyEvent(event);
		if (value)
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (videoView != null) {
			videoView.stopPlayback();
			videoView.release(true);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DoPolyvConstant.ijkVideo = null;
		if (videoView != null) {
			videoView.stopPlayback();
			videoView.release(true);
		}
	}

	@Override
	public JSONObject getState() {
		JSONObject obj = new JSONObject();
		if (videoView != null) {
			int state = videoView.isPlaying() ? 1 : 0;
			try {
				obj.put("state", state);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return obj;
	}

	@Override
	public void stop() {
		if (videoView != null) {
			videoView.stopPlayback();
			videoView.release(true);
			finish();
		}
	}

	@Override
	public int getCurrentTime() {
		if (videoView != null) {
			return videoView.getCurrentPosition();
		}
		return 0;
	}
}
