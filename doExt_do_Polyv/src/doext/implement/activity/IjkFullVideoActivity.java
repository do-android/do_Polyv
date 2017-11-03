package doext.implement.activity;

import org.json.JSONException;
import org.json.JSONObject;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.easefun.polyvsdk.ijk.IjkVideoView;
import com.easefun.polyvsdk.ijk.OnPreparedListener;

import core.helper.DoResourcesHelper;
import doext.implement.DoPolyvConstant;
import doext.implement.custom.MediaController;
import doext.implement.inteface.IDojkVideo;

public class IjkFullVideoActivity extends Activity implements IDojkVideo {
	private IjkVideoView videoView;
	private MediaController mediaController;
	private ProgressBar progressBar;
	private int stopPosition = 0;
	private int bitRate = 0;
	private String path;
	private String vid;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		DoPolyvConstant.ijkVideo = this;
		int do_polyv_video_small = DoResourcesHelper.getIdentifierByStr("do_polyv_video_small", "layout", DoPolyvConstant.TYPEID);
		setContentView(do_polyv_video_small);
		Bundle e = getIntent().getExtras();
		if (e != null) {
			path = e.getString("path");
			vid = e.getString("vid");
			stopPosition = e.getInt("position");
			bitRate = e.getInt("bitRate");
		}

		int videoview_id = DoResourcesHelper.getIdentifierByStr("videoview", "id", DoPolyvConstant.TYPEID);
		videoView = (IjkVideoView) findViewById(videoview_id);
		int loadingprogress_id = DoResourcesHelper.getIdentifierByStr("loadingprogress", "id", DoPolyvConstant.TYPEID);
		progressBar = (ProgressBar) findViewById(loadingprogress_id);

		mediaController = new MediaController(this, false);
		mediaController.setIjkVideoView(videoView);
		videoView.setMediaController(mediaController);
		videoView.setMediaBufferingIndicator(progressBar);
		if (path != null && path.length() > 0) {
			progressBar.setVisibility(View.GONE);
			videoView.setVideoURI(Uri.parse(path));
		} else {
			if (bitRate == 0) {
				videoView.setVid(vid);
			} else {
				videoView.setVid(vid, bitRate);
			}
		}
		videoView.setVideoLayout(IjkVideoView.VIDEO_LAYOUT_ZOOM);
		videoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(IMediaPlayer mp) {
				videoView.setVideoLayout(IjkVideoView.VIDEO_LAYOUT_ZOOM);
				if (stopPosition > 0) {
					videoView.seekTo(stopPosition);
				}
			}
		});

		// 设置视频尺寸 ，在横屏下效果较明显
		mediaController.setOnVideoChangeListener(new MediaController.OnVideoChangeListener() {
			@Override
			public void onVideoChange(int layout) {
				videoView.setVideoLayout(layout);
				switch (layout) {
				case IjkVideoView.VIDEO_LAYOUT_ORIGIN:
					Toast.makeText(IjkFullVideoActivity.this, "VIDEO_LAYOUT_ORIGIN", Toast.LENGTH_SHORT).show();
					break;
				case IjkVideoView.VIDEO_LAYOUT_SCALE:
					Toast.makeText(IjkFullVideoActivity.this, "VIDEO_LAYOUT_SCALE", Toast.LENGTH_SHORT).show();
					break;
				case IjkVideoView.VIDEO_LAYOUT_STRETCH:
					Toast.makeText(IjkFullVideoActivity.this, "VIDEO_LAYOUT_STRETCH", Toast.LENGTH_SHORT).show();
					break;
				case IjkVideoView.VIDEO_LAYOUT_ZOOM:
					Toast.makeText(IjkFullVideoActivity.this, "VIDEO_LAYOUT_ZOOM", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		});
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
