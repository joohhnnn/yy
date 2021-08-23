#ifndef __TXZ_LICENSE_H__
#define __TXZ_LICENSE_H__

#include<string>
#include <unistd.h>
#include <stdio.h>
#include<android/log.h>
#include "json.h"
#include "txz_license_interface.h"

using namespace std;

typedef struct LicenseReq
{
	std::string mHeaderJson;
	std::string mBodyJson;
	OnLicense mOnEventPtr;
} LicenseReq_t;

typedef struct LicenseResp
{
	int type;//0:unkown, 1:本地，2:在线
	std::string mStrBody;
	int errCode;
} LicenseResp_t;


typedef struct EquipmentInfo
{
	::Json::Value jsonEquipmentInfo;
} EquipmentInfo_t;

enum{
	LICENSE_TYPE_UNKOWN = 0,
	LICENSE_TYPE_LOCAL = 1,
	LICENSE_TYPE_ONLINE = 2,
};

void do_license(LicenseReq_t& req);

void do_login(LicenseReq_t& req);

bool checkLocalLicense(const std::string& sLicenseFilePath, const ::Json::Value& param, const std::string& sPassword, ::Json::Value& equipmentInfo);

bool writeLocalLicense(const std::string& sLicenseFilePath, const ::Json::Value& equipmentInfo, const std::string& sPassword);

LicenseResp_t doLicense(LicenseReq_t& req);

LicenseResp_t doLogin(LicenseReq_t& req);

LicenseResp_t requestLicense(const ::Json::Value& jsonReqHeader, const ::Json::Value& jsonReqBody, bool isLicenseReq);

#endif

