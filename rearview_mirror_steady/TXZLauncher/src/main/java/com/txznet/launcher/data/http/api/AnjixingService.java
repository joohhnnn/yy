package com.txznet.launcher.data.http.api;

import com.txznet.launcher.data.entity.BaseResp;
import com.txznet.launcher.data.entity.BindInfoResp;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * 安吉星api定义
 */
public interface AnjixingService {


    /*

    绑定接口
    https://idt10.onstar.com.cn/partner/sos/rvmirror/v1/external/screen/2143566035291202577873
    ==========绑定前
    404 {"errorCode":"E4004","errorMsg":"无法找到请求的资源"}


    ==========绑定后
    200 {"vehicleInfo":{"vehicleLicense":"沪A12345"},"userInfo":{"name":"牛德华","birthday":"1988-10-01"}}


    解绑接口
    https://idt10.onstar.com.cn/partner/sos/rvmirror/v1/external/screen/2143566035291202577873?opt=unbinding

    ====== 未绑定
    404 {"errorCode":"E4004","errorMsg":"无法找到请求的资源"}

    ====== 已绑定
    200 {"errorCode":"E0000","errorMsg":"成功"}
     */


    /**
     * 获取后视镜后视镜绑定定用户及设备信息
     *
     * @param deviceID CoreID+IMEI
     */
    @GET("/partner/sos/rvmirror/v1/external/screen/{DeviceID}")
//    @Headers("Content-Type: Synchronous")
    Flowable<BindInfoResp> getBindInfo(@Path("DeviceID") String deviceID);

    /**
     * 对指定后视镜的用户绑定关系进行解除
     *
     * @param deviceID CoreID+IMEI
     */
    @POST("/partner/sos/rvmirror/v1/external/screen/{DeviceID}?opt=unbinding")
    Flowable<BaseResp> unbind(@Path("DeviceID") String deviceID);
}
