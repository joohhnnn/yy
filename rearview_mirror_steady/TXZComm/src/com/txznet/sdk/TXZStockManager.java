package com.txznet.sdk;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.StockInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class TXZStockManager {

    public static final String STOCK_CMD_PREFIX = "txz.stock.cmd.";//core->sdk
    public static final String STOCK_INVOKE_PREFIX = "txz.stock.invoke.";//sdk->core
    public static final String SET_TIMEOUT = "setTimeout";
    public static final String SET_STOCK_TOOL = "setTool";
    public static final String CLEAR_STOCK_TOOL = "clearTool";
    public static final String REQUEST_STOCK = "request";
    public static final String RESULT_STOCK = "result";
    public static final String ERROR_STOCK = "error";
    private Long mTimeout = null;

    private static TXZStockManager sInstance = new TXZStockManager();

    private TXZStockManager() {
    }

    public static TXZStockManager getInstance() {
        return sInstance;
    }

    void onReconnectTXZ() {
        if (mTimeout != null) {
            setTimeout(mTimeout);
        }

        if (mStockTool != null) {
            setStockTool(mStockTool);
        }
    }

    /**
     * 设置请求超时时间
     *
     * @param timeout
     */
    public void setTimeout(long timeout) {
        mTimeout = timeout;
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("timeout", timeout);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, STOCK_INVOKE_PREFIX + SET_TIMEOUT, jsonBuilder.toBytes(), null);
    }

    StockTool mStockTool;

    /**
     * 设置股票工具
     *
     * @param stockTool
     */
    public void setStockTool(StockTool stockTool) {
        mStockTool = stockTool;
        if (mStockTool == null) {
            TXZService.setCommandProcessor(STOCK_CMD_PREFIX, null);
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, STOCK_INVOKE_PREFIX + CLEAR_STOCK_TOOL, null, null);
        } else {
            TXZService.setCommandProcessor(STOCK_CMD_PREFIX, mCommandProcessor);
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, STOCK_INVOKE_PREFIX + SET_STOCK_TOOL, null, null);
        }

    }

    private TXZService.CommandProcessor mCommandProcessor = new TXZService.CommandProcessor() {
        @Override
        public byte[] process(String packageName, String command, byte[] data) {
            if (TextUtils.equals(REQUEST_STOCK, command)) {
                if (mStockTool != null) {
                    JSONBuilder jsonBuilder = new JSONBuilder(data);
                    String taskId = jsonBuilder.getVal("taskid", String.class);
                    String stockCode = jsonBuilder.getVal("stockCode", String.class);
                    mStockTool.requestStock(stockCode,new StockRequestListener(taskId));
                }
            }
            return null;
        }
    };

    /**
     * 股票数据获取工具
     */
    public static interface StockTool {
        /**
         * @param code                   股票代码
         * @param stockRequestListener 当次请求结果回调的接口
         */
        public void requestStock(String code, StockRequestListener stockRequestListener);
    }

    /**
     * 天气请求的回调接口，
     * 至少需要返回3天的天气数据
     */
    public static class StockRequestListener {
        private String mTaskId;
        private boolean mOnFinish = false;

        public StockRequestListener(String mTaskId) {
            this.mTaskId = mTaskId;
        }

        /**
         * 股票数据
         *
         * @param StockInfo 股票数据
         */
        public synchronized void onResult(StockInfo StockInfo) {
            if (!mOnFinish) {
                mOnFinish = true;
                if (StockInfo != null) {
                    JSONBuilder jsonBuilder = parseStockInfo(StockInfo);
                    if(jsonBuilder == null){
                        onError("data onError");
                        return;
                    }
                    jsonBuilder.put("taskid", mTaskId);
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, STOCK_INVOKE_PREFIX + RESULT_STOCK, jsonBuilder.toBytes(), null);
                } else {
                    JSONBuilder jsonBuilder = new JSONBuilder();
                    jsonBuilder.put("taskid", mTaskId);
                    jsonBuilder.put("msg", "data error");
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, STOCK_INVOKE_PREFIX + ERROR_STOCK, jsonBuilder.toBytes(), null);
                }
            }
        }

        /**
         * 请求天气失败时的回调
         *
         * @param msg
         */
        public synchronized void onError(String msg) {
            if (!mOnFinish) {
                mOnFinish = true;
                JSONBuilder jsonBuilder = new JSONBuilder();
                jsonBuilder.put("taskid", mTaskId);
                jsonBuilder.put("msg", msg);
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, STOCK_INVOKE_PREFIX + ERROR_STOCK, jsonBuilder.toBytes(), null);
            }
        }

        private JSONBuilder parseStockInfo(StockInfo StockInfo){
            JSONBuilder jsonBuilder = new JSONBuilder();
            JSONObject data = new JSONObject();
            jsonBuilder.put("data",data);
            JSONObject result = new JSONObject();
            try {
                data.put("result",result);
                result.put("mName",StockInfo.strName);
                result.put("mCode",StockInfo.strCode);
                result.put("mChartImgUrl",StockInfo.strUrl);
                result.put("mCurrentPrice",StockInfo.strCurrentPrice);
                result.put("mChangeAmount",StockInfo.strChangeAmount);
                result.put("mChangeRate",StockInfo.strChangeRate);
                result.put("mHighestPrice",StockInfo.strHighestPrice);
                result.put("mLowestPrice",StockInfo.strLowestPrice);
                result.put("mtradingVolume",StockInfo.strTradingVolume);
                result.put("mUpdateTime",StockInfo.strUpdateTime);
                result.put("mYesterdayClosingPrice",StockInfo.strYestodayClosePrice);
                result.put("mTodayOpeningPrice",StockInfo.strTodayOpenPrice);
            } catch (JSONException e) {
               return null;
            }
            return jsonBuilder;
        }
    }
}
