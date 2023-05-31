package com.example.fxt.ble.api.core;

/**
 * 发送命令回调
 */
interface BleSendCallback {

	/**
	 * 指令发送成功时
     */
	void onSuccess();
	/**
	 * 失败时
	 * @param code 错误编码
	 * @param errMsg 错误信息
     */
	void onFail(final int code, final String errMsg);
}
