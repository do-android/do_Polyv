package doext.implement.service;

import android.content.Intent;

import com.easefun.polyvsdk.server.AndroidService;

public class DoPolyvService extends AndroidService {

	// 无参数构造函数，调用父类的super(String name)
	public DoPolyvService() {
		super();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		super.onHandleIntent(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
