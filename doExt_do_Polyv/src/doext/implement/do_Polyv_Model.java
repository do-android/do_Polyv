package doext.implement;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import com.easefun.polyvsdk.BitRateEnum;
import com.easefun.polyvsdk.PolyvDownloadProgressListener;
import com.easefun.polyvsdk.PolyvDownloader;
import com.easefun.polyvsdk.PolyvDownloaderErrorReason;
import com.easefun.polyvsdk.PolyvDownloaderManager;
import com.easefun.polyvsdk.PolyvSDKClient;
import com.easefun.polyvsdk.RestVO;
import com.easefun.polyvsdk.Video;

import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.interfaces.DoIModuleTypeID;
import core.interfaces.DoIScriptEngine;
import core.object.DoEventCenter;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.define.do_Polyv_IMethod;
import doext.implement.activity.IjkFullVideoActivity;
import doext.implement.activity.IjkVideoActicity;
import doext.implement.service.DoPolyvService;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_Polyv_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_Polyv_Model extends DoSingletonModule implements do_Polyv_IMethod, DoIModuleTypeID {

	private Context mContext;

	public do_Polyv_Model() throws Exception {
		super();
		this.mContext = DoServiceContainer.getPageViewFactory().getAppContext();
		String _sdkStr = this.mContext.getPackageManager().getApplicationInfo(this.mContext.getPackageName(), PackageManager.GET_META_DATA).metaData.getString("DoPolyvSdkstr");
		if (TextUtils.isEmpty(_sdkStr)) {
			throw new Exception("DoPolyvSdkstr 的值不能为空！请在后台配置");
		}
		initPolyvCilent(_sdkStr);
	}

	private void initPolyvCilent(String _sdkStr) {
		File saveDir = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			saveDir = new File(DoServiceContainer.getGlobal().getDataRootPath(), DoServiceContainer.getGlobal().getMainAppID() + "/polyvdownload");
			if (saveDir.exists() == false)
				saveDir.mkdir();
		}
		PolyvSDKClient client = PolyvSDKClient.getInstance();
		// 设置SDK加密串
//		client.setConfig("esko4U+EtlhE4kr44Ir67O4y/7G8ftFXnm9KUR6wA7m4UOhIOX6keTMu+Ep0XByGrX0DfQ247HQ1/IEw6qkNWRuVEOTWl8pdSTEWl+9SGEQRaEe0XNRtVR3oHV1pE8jbgj4Rw8Cc0pPf5w85JoF8Nw==");
		client.setConfig(_sdkStr);
		// 下载文件的目录
		client.setDownloadDir(saveDir);
		// 初始化数据库服务
		client.initDatabaseService(mContext);
		// 启动服务
		client.startService(mContext.getApplicationContext(), DoPolyvService.class);
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("play".equals(_methodName)) {
			this.play(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("stop".equals(_methodName)) {
			this.stop(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("getState".equals(_methodName)) {
			this.getState(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("getCurrentTime".equals(_methodName)) {
			this.getCurrentTime(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("download".equals(_methodName)) {
			this.download(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		if ("getList".equals(_methodName)) {
			this.getList(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("getInfo".equals(_methodName)) {
			this.getInfo(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 下载视频；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void download(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {

		String _id = DoJsonHelper.getString(_dictParas, "id", "");
		if (TextUtils.isEmpty(_id)) {
			throw new Exception("id不能为空！");
		}
		int _bitRate = DoJsonHelper.getInt(_dictParas, "bitRate", 0);

		PolyvDownloader downloader = PolyvDownloaderManager.getPolyvDownloader(_id, _bitRate);

		downloader.setPolyvDownloadProressListener(new PolyvDownloadProgressListener() {
			public void onDownload(long downloaded, long total) {
				firedownloadProgress(total, downloaded);
			}

			public void onDownloadSuccess() {
				DoInvokeResult _result = new DoInvokeResult(getUniqueKey());
				JSONObject _obj = new JSONObject();
				try {
					_obj.put("type", 0);
					_result.setResultNode(_obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				fireEvent("success", _result);
			}

			public void onDownloadFail(PolyvDownloaderErrorReason errorReason) {
				DoInvokeResult _result = new DoInvokeResult(getUniqueKey());
				JSONObject _obj = new JSONObject();
				try {
					_obj.put("type", 0);
					_obj.put("message", errorReason.getCause().getMessage());
					_result.setResultNode(_obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				fireEvent("fail", _result);
			}
		});

		downloader.start();
	}

	private void firedownloadProgress(long total, long curr) {
		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		JSONObject jsonNode = new JSONObject();
		try {
			jsonNode.put("progress", (curr * 100) / (double) total);
			_invokeResult.setResultNode(jsonNode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fireEvent("downloadProgress", _invokeResult);
	}

	private void fireEvent(String eventName, DoInvokeResult _invokeResult) {
		DoEventCenter eventCenter = getEventCenter();
		if (eventCenter != null) {
			if (_invokeResult == null) {
				_invokeResult = new DoInvokeResult(getUniqueKey());
			}
			eventCenter.fireEvent(eventName, _invokeResult);
		}
	}

	/**
	 * 获取当前视频播放时间；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void getCurrentTime(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		int _currentTime = 0;
		if (null != DoPolyvConstant.ijkVideo) {
			_currentTime = DoPolyvConstant.ijkVideo.getCurrentTime();
		}
		_invokeResult.setResultInteger(_currentTime);
	}

	/**
	 * 获取视频信息；
	 * 
	 * @throws JSONException
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void getInfo(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final String _callbackFuncName) throws Exception {
		String _id = DoJsonHelper.getString(_dictParas, "id", "");
		if (TextUtils.isEmpty(_id)) {
			throw new Exception("id不能为空！");
		}
		final DoInvokeResult _result = new DoInvokeResult(getUniqueKey());
		//{id:'唯一标识',title:'标题',duration:'时长',imageUrl:'预览图片地址',fileSize:'文件大小(字节)','bitRates':'支持的码率'}
//		SDKUtil.loadVideoJSON2Video(_id, false);
		Video.loadVideo(_id, new Video.OnVideoLoaded() {
			public void onloaded(Video v) {
				JSONObject _obj = new JSONObject();
				String _id = v.getVid();
				String _duration = v.getDuration();
				String _imageUrl = v.getFirstImage();
				List<Long> _fileSize = v.getFileSize();
				String[] _bitRates = BitRateEnum.getBitRateNameArray(v.getDfNum());
				try {
					_obj.put("id", _id);
					_obj.put("duration", _duration);
					_obj.put("imageUrl", _imageUrl);
					_obj.put("fileSize", _fileSize);
					_obj.put("bitRates", Arrays.toString(_bitRates));

					_result.setResultNode(_obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				_scriptEngine.callback(_callbackFuncName, _result);
			}
		});
	}

	/**
	 * 获取视频列表；
	 * 
	 * @throws JSONException
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void getList(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws JSONException {

		int _pageIndex = DoJsonHelper.getInt(_dictParas, "pageIndex", 0);
		int _pageSize = DoJsonHelper.getInt(_dictParas, "pageSize", 10);
		//api 提供是从1开始，为了兼容
		_pageIndex++;

		List<RestVO> _restVOs = PolyvSDKClient.getInstance().getVideoList(_pageIndex, _pageSize);
		final DoInvokeResult _result = new DoInvokeResult(getUniqueKey());
		//{id:'唯一标识',title:'标题',duration:'时长',imageUrl:'预览图片地址',fileSize:'文件大小(字节)','bitRates':'支持的码率'}
		JSONArray _array = new JSONArray();
		for (RestVO _restVO : _restVOs) {

			String _id = _restVO.getVid();
			String _title = _restVO.getTitle();
			String _duration = _restVO.getDuration();
			String _imageUrl = _restVO.getFirstImage();
//			long _fileSize = _restVO.getSourceFileSize();
			List<Long> _fileSize = _restVO.getFileSize();
			String[] _bitRates = BitRateEnum.getBitRateNameArray(_restVO.getDf());
			JSONObject _obj = new JSONObject();
			try {
				_obj.put("id", _id);
				_obj.put("title", _title);
				_obj.put("duration", _duration);
				_obj.put("imageUrl", _imageUrl);
				_obj.put("fileSize", _fileSize);
				_obj.put("bitRates", Arrays.toString(_bitRates));

				_array.put(_obj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		_result.setResultArray(_array);
		_scriptEngine.callback(_callbackFuncName, _result);
	}

	/**
	 * 获取视频状态；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void getState(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		JSONObject _result = null;
		if (null != DoPolyvConstant.ijkVideo) {
			_result = DoPolyvConstant.ijkVideo.getState();
		}
		_invokeResult.setResultNode(_result);
	}

	/**
	 * 播放视频；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void play(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _id = DoJsonHelper.getString(_dictParas, "id", "");
		if (TextUtils.isEmpty(_id)) {
			throw new Exception("id不能为空！");
		}
		int _point = DoJsonHelper.getInt(_dictParas, "point", 0);
		boolean _isFull = DoJsonHelper.getBoolean(_dictParas, "isFull", false);
		int _bitRate = DoJsonHelper.getInt(_dictParas, "bitRate", 0);
		Intent intent = null;
		if (_isFull) {
			intent = new Intent(mContext, IjkFullVideoActivity.class);
		} else {
			intent = new Intent(mContext, IjkVideoActicity.class);
		}
		intent.putExtra("vid", _id);
		intent.putExtra("position", _point);
		intent.putExtra("bitRate", _bitRate);
		((Activity) mContext).startActivityForResult(intent, 1);
	}

	/**
	 * 停止播放；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void stop(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if (null != DoPolyvConstant.ijkVideo) {
			DoPolyvConstant.ijkVideo.stop();
		}
	}
}