package org.hdu.back.controller.base;

import java.util.HashMap;
import java.util.Map;

/**
 * 控制层基类
 * 
 * @author liuchao
 * 
 */
public class BaseController {
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_BUSINESS_ERROR = -1;
	public static final int CODE_SERVER_ERROR = -2;
	public static final int CODE_LOGIN_ERROR = -3;
	public static final int CODE_LOGIN_ERROR_1 = -400001;
	public static final int CODE_LOGIN_ERROR_2 = -400002;
	public static final int CODE_LOGIN_ERROR_3 = -400003;
	public static final int CODE_LOGIN_ERROR_4 = -400004;
	

	/**
	 * 返回数据通用方法
	 * 
	 * @param code
	 * @param msg
	 * @param key
	 * @param value
	 * @return
	 */
	protected static Map<String, Object> buildResult(int code, String msg, String key,
			Object value) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", code);
		result.put("msg", msg);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(key, value);
		result.put("data", data);
		return result;
	}

	/**
	 * 返回成功数据通用方法
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	protected static Map<String, Object> buildResult(String key, Object value) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", CODE_SUCCESS);
		result.put("msg", "");
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(key, value);
		result.put("data", data);
		return result;
	}

	/**
	 * 返回成功数据通用方法
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	protected static Map<String, Object> buildResult(Map data) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", CODE_SUCCESS);
		result.put("msg", "");
		result.put("data", data);
		return result;
	}
	
	/**
	 * 返回异常数据通用方法
	 * 
	 * @param code
	 * @param msg
	 * @return
	 */
	protected static Map<String, Object> buildResult(int code, String msg) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", code);
		result.put("msg", msg);
		Map<String, Object> data = new HashMap<String, Object>();
		result.put("data", data);
		return result;
	}
	
	protected static Map<String, Object> buildResult(int code, Object msg) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", code);
		result.put("msg", msg);
		Map<String, Object> data = new HashMap<String, Object>();
		result.put("data", data);
		return result;
	}
}
