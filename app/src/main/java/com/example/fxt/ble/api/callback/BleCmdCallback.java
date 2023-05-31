package com.example.fxt.ble.api.callback;


import com.example.fxt.ble.api.event.ResultEvent;
import com.example.fxt.ble.api.event.WriteEvent;

/**
 * 发送命令回调
 */
public interface BleCmdCallback {

	/**
	 * 指令发送时回调
     */
	void onCmdWrite(WriteEvent event);

	/**
	 * 指令接受到时回调
     */
	void onReceiveResult(ResultEvent event);

	/**
	 * 蓝牙设备回馈是否结束（主要在指纹录入时会有多条反馈）
     */
	 boolean isNeedReceiveMore(ResultEvent event);

	void onSend(String str);
}
