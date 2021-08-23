package com.txznet.txz.util;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * -2.0 ---------- -4.0
 * -1.2 ---------- -3.1
 * -0.8 ---------- -2.9
 * +1.0 --------- -2.7
 * +2.0 --------- -2.
 */
public class ThreshHoldAdapter {
	private final static float FLOAT_ACCURACY = 0.0001f; 
	public static float setThreshValueToV3(float value){
		return value;
	}
	
	public static float getThreshValueFromV3(float value){
		float originVal = -3.1f + FLOAT_ACCURACY;
		//由于是float类型，需要考虑相等情况的特殊判断
		if (value > 7.0 - FLOAT_ACCURACY){//大于等于7.0时
			originVal = -1.3f;
		}else if (value > 6.0 - FLOAT_ACCURACY){//大于等于6.0时
			originVal = -1.5f;
		}else if (value > 5.0 - FLOAT_ACCURACY){//大于等于5.0时
			originVal = -1.7f;
		}else if (value > 4.0 - FLOAT_ACCURACY){//大于等于4.0时
			originVal = -1.9f;
		}else if (value > 3.0 - FLOAT_ACCURACY){//大于等于3.0时
			originVal = -2.1f;
		}else if (value > 2.5 - FLOAT_ACCURACY){//大于等于2.5时
			originVal = -2.2f;
		}else if (value > 2.0 - FLOAT_ACCURACY){//大于等于2.0时
			originVal = -2.3f;
		}else if (value > 1.5 - FLOAT_ACCURACY){//大于等于1.5时
			originVal = -2.4f;
		}else if (value > 1.0 - FLOAT_ACCURACY){//大于等于1.0时
			originVal = -2.5f;
		}else if (value > 0.0 - FLOAT_ACCURACY){//大于等于0.0时
			originVal = -2.6f;
		}else if (value > -1.3 - FLOAT_ACCURACY){//大于等于-1.3时
			originVal = -2.7f;
		}else if (value > -2.3 - FLOAT_ACCURACY){//大于等于-2.3时
			originVal = -2.8f;
		}else if (value > -3.3 - FLOAT_ACCURACY){//大于等于-3.3时
			originVal = -2.9f;
		}else if (value > -4.3 - FLOAT_ACCURACY){//大于等于-4.3时// 74版本标准阈值-4.3f
			originVal = -3.1f;
		}else if (value > -5.0 - FLOAT_ACCURACY){//大于等于-5.0时/
			originVal = -3.2f;
		}else if (value > -5.8 - FLOAT_ACCURACY){//大于等于-5.8时
			originVal = -3.3f;
		}else if (value > -6.0 - FLOAT_ACCURACY){//大于等于-6.0时
			originVal = -3.5f;
		}else{
			originVal = -5.0f;
		}
		
		/****************************重要********************************/
		/******上层比较阈值的时候,没有考虑到float类型相等的情况****/
		/******最终转换的值要比临界值大一点点，否则没法唤醒*******/
		originVal += 0.01f;
		/****************************重要********************************/
		
		return originVal;
	}
	
	//[{"keyWords":"可乐","threshold":-3.1},{"keyWords":"雪碧","threshold":-3.2},{"keyWords":"美年达","threshold":-3.3}]
	public static String genKwsThreshValue(){
		JSONArray array = new JSONArray();
		//单独调整特定一个唤醒词的阈值
		{
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("keyWords", "上一首");
				jsonObj.put("threshold", -2.5f);
				array.put(jsonObj);
			} catch (Exception e) {
			}
		}
		
		//单独调整特定一个唤醒词的阈值
		{
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("keyWords", "下一首");
				jsonObj.put("threshold", -2.5f);
				array.put(jsonObj);
			} catch (Exception e) {
			}
		}
		
		//单独调整特定一个唤醒词的阈值
		{
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("keyWords", "确定");
				jsonObj.put("threshold", -3.1f);
				array.put(jsonObj);
			} catch (Exception e) {
			}
		}
		
		//单独调整特定一个唤醒词的阈值
		{
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("keyWords", "取消");
				jsonObj.put("threshold", -3.1f);
				array.put(jsonObj);
			} catch (Exception e) {
			}
		}
		
		//单独调整特定一个唤醒词的阈值
		{
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("keyWords", "再见");
				jsonObj.put("threshold", -3.1f);
				array.put(jsonObj);
			} catch (Exception e) {
			}
		}
		
		//单独调整特定一个唤醒词的阈值
		{
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("keyWords", "上翻");
				jsonObj.put("threshold", -3.1f);
				array.put(jsonObj);
			} catch (Exception e) {
			}
		}
		
		//单独调整特定一个唤醒词的阈值
		{
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("keyWords", "下翻");
				jsonObj.put("threshold", -3.1f);
				array.put(jsonObj);
			} catch (Exception e) {
			}
		}
		
		//单独调整特定一个唤醒词的阈值
		{
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("keyWords", "接听");
				jsonObj.put("threshold", -3.1f);
				array.put(jsonObj);
			} catch (Exception e) {
			}
		}
		
		//单独调整特定一个唤醒词的阈值
		{
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("keyWords", "挂断");
				jsonObj.put("threshold", -3.1f);
				array.put(jsonObj);
			} catch (Exception e) {
			}
		}

		//单独调整特定一个唤醒词的阈值
		{
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("keyWords", "返回");
				jsonObj.put("threshold", -3.1f);
				array.put(jsonObj);
			} catch (Exception e) {
			}
		}
		
		return array.toString();
	}
	
	public final static float SHORT_WORD_THRESH= -2.70f;
}
