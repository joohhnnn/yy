package com.txznet.launcher.data.entity;

/**
 * 获取后视镜绑定用户及设备信息 返回的json结果
 */
public class BindInfoResp extends BaseResp{

    public VehicleInfo vehicleInfo;
    public UserInfo userInfo;


    /*
     " vehicleInfo": {
       "vehicleLicense": "沪A12345"
        },
     */
    public static class VehicleInfo {
        public String vehicleLicense;

        @Override
        public String toString() {
            return "VehicleInfo{" +
                    "vehicleLicense='" + vehicleLicense + '\'' +
                    '}';
        }
    }


    /*
         " userInfo": {
              "birthday": "2000-01-01",
                "name": "小明"
            },
     */
    public static class UserInfo {
        public String birthday;
        public String name;

        @Override
        public String toString() {
            return "UserInfo{" +
                    "birthday='" + birthday + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }


    @Override
    public String toString() {
        return "BindInfoResp{" +
                "vehicleInfo=" + vehicleInfo +
                ", userInfo=" + userInfo +
                ", errorCode='" + errorCode + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
