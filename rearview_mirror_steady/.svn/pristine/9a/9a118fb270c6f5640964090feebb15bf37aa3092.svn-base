#include "txz_license_interface.h"
#include "txz_license.h"
#include  "curl.h"
#include "json.h"
#include "utils.h"

#include<string>
#include <sys/types.h>
#include <pwd.h>
#include<stdio.h>
#include "txz_file_util.h"
#include <vector>
#include "time_utils.h"
#include<pthread.h>
#include <unistd.h>

 using namespace std;

 static EquipmentInfo g_EquipmentInfo;

 extern "C"  int getpwuid_r(uid_t uid, struct passwd *pwd,
         char *buf, size_t buflen, struct passwd **result){
	 struct passwd * res = getpwuid(uid);
	 *pwd = *res;
	 *result = pwd;
	 return 0;
 }

 void* run_license(void* args)
 {

	 int sleepSec = 3;
	 int count = 0;

	 while (true)
	 {
		LicenseReq_t* req = (LicenseReq_t*) args;
		LicenseResp_t resp;
		resp = doLicense(*req);

		if (resp.errCode == 0) {
			//notify
			if (req->mOnEventPtr != NULL)
			{
				req->mOnEventPtr(0, resp.mStrBody);
			}
			//非在线授权成功，则发起登录请求
			if (resp.type != LICENSE_TYPE_ONLINE)
			{
				do_login(*req);
			}
			return NULL;
		}
		count++;
		if (count > 30)
		{
			return NULL;
		}

		if (req->mOnEventPtr != NULL)
		{
			req->mOnEventPtr(resp.errCode, "");
		}

		LOGD("errCode: %d", resp.errCode);
		switch (resp.errCode)
		{
		case 21003:
		case 21001:
		case 21002: // 重复激活
		case 10003: // 请求头签名校验失败
		case 10002: // 请求头参数错误
		case 10001: // 请求头缺失需要字段
			//notify error
				return NULL;
		case 12004: // SQL 查询失败
		case 12003: // SQL 初始化错误
		case 11003: // 包体解压错误
		case 11002: // 包体参数错误
		case 11001: // 包体缺失必须字段
		case 14: // 参数错误
		case 1: // 繁忙
		default:
			;
		}
		sleepSec = sleepSec > 60 ? 60 : 2 * sleepSec;
		sleep(sleepSec);
	}
	 return NULL;
 }

 void* run_login(void* args)
 {

	 int sleepSec = 3;
	 int count = 0;
	 while (true)
	 {
		LicenseReq_t* req = (LicenseReq_t*) args;
		LicenseResp_t resp;
		resp = doLogin(*req);

		if (resp.errCode == 0) {
			return NULL;
		}

		count++;
		if (count > 30)
		{
			return NULL;
		}

		sleepSec = sleepSec > 60 ? 60 : 2 * sleepSec;

		sleep(sleepSec);
	}
	 return NULL;
 }

 LicenseReq g_Req;

void txz_license(const std::string& headerJson, const std::string& bodyJson, OnLicense ptr)
{
	g_Req.mHeaderJson.assign(headerJson);
	g_Req.mBodyJson.assign(bodyJson);
	g_Req.mOnEventPtr = ptr;
	do_license(g_Req);
}

void do_license(LicenseReq_t& req)
{
	//init_sigaction(timer_func);
	//txz_settimer(1000);
	pthread_t thread_1;
	pthread_create(&thread_1,NULL,run_license, &req);
}

void do_login(LicenseReq_t& req)
{
	//init_sigaction(timer_func);
	//txz_settimer(1000);
	pthread_t thread_1;
	pthread_create(&thread_1,NULL,run_login, &req);
}

size_t my_write_func(void *ptr, size_t size, size_t nmemb, void* userdata)
{
	std::string* s = (std::string*)userdata;
	size_t writeSize = size*nmemb;
	s->append((char*)ptr, writeSize);
	return writeSize;
}
/*
LicenseInfoFile:
{
	"uid":"111",
	"data":"加密的LicenseInfo"
}
LicenseInfo
{
	"appid":"222",
	"hash":"aaa",
     "other":"bbb"
}
*/
bool checkLocalLicense(const std::string& sLicenseFilePath, const ::Json::Value& param, const std::string& sPassword, ::Json::Value& equipmentInfo)
{
	std::string sFileContent;
    FileUtil::readFile(sLicenseFilePath, sFileContent);
	::Json::Value jsonLicenseFileContent;
    ::Json::Reader oReader;
    if (!oReader.parse(sFileContent, jsonLicenseFileContent, false))
    {
    	return false;
    }

    std::string encrpytedLicense = jsonLicenseFileContent["data"].asString();

    std::string decrpytedLicense;
    decrpyt(encrpytedLicense, decrpytedLicense, sPassword);

    ::Json::Value jsonLicense;

    if (!oReader.parse(decrpytedLicense, jsonLicense, false))
    {
    	return false;
    }

    std::string licenseAppId =jsonLicense["appid"].asString();
    std::string appid = param["appid"].asString();

    if (appid == licenseAppId)
    {
    	equipmentInfo = jsonLicenseFileContent;
    	equipmentInfo["data"] = jsonLicense;
    	return true;
    }
	return false;
}

bool writeLocalLicense(const std::string& sLicenseFilePath, const ::Json::Value& equipmentInfo, const std::string& sPassword)
{
	 ::Json::Value fileContent;
	 fileContent = equipmentInfo;

	std::string sFileContent;
	std::string encrpytedLicense;
	equipmentInfo["data"];

	std::string s = equipmentInfo["data"].toStyledString();

	encrpyt(s, encrpytedLicense, sPassword);

	fileContent["data"] = encrpytedLicense;

    FileUtil::saveFile(sLicenseFilePath, fileContent.toStyledString());

    return true;
}


LicenseResp_t doLicense(LicenseReq_t& req)
{
	LOGD("doLicense");
	LicenseResp_t resp;
	resp.type = LICENSE_TYPE_UNKOWN;

	::Json::Value jsonReqBody;
	::Json::Reader oReader;
	if (!oReader.parse(req.mBodyJson, jsonReqBody, false)) {

	}

	::Json::Value jsonReqHeader;
	if (!oReader.parse(req.mHeaderJson, jsonReqHeader, false)) {

	}

	::Json::Value param;
	param["appid"] = jsonReqHeader["COMP-APPID"].asString();
	param["android_id"] = jsonReqBody["android_id"].asString();
	param["uuid"] = jsonReqBody["uuid"].asString();
	std::string android_id = param["android_id"].asString();
	std::string uuid = param["uuid"].asString();
	std::string appid = param["appid"].asString();
	std::string sPassword;
	sPassword = md5(appid + uuid + android_id);

	::Json::Value& equipmentInfo = g_EquipmentInfo.jsonEquipmentInfo;

	bool licenseOk = false;
	std::vector<std::string> stEquipmentInfoPaths;
	stEquipmentInfoPaths.push_back("/data/data/com.txznet.txz/.Config.dat");
	stEquipmentInfoPaths.push_back("/sdcard/txz/.Config.dat");


	do {
		//首先进行离线授权校验
		resp.type = LICENSE_TYPE_LOCAL;
		for (int i = 0; i < stEquipmentInfoPaths.size(); ++i)
		{
			std::string path = stEquipmentInfoPaths[i];
			licenseOk = checkLocalLicense(path, param, sPassword, equipmentInfo);

			if (licenseOk) {
				LOGD("license : %s", equipmentInfo.toStyledString().c_str());
				break;
			}
		}
		//离线授权校验成功
		if (licenseOk)
		{
			resp.errCode = 0;
			break;
		}

		//进行在线授权校验
		resp.type = LICENSE_TYPE_ONLINE;
		jsonReqBody["hash"] = equipmentInfo["data"]["hash"];//

		::Json::Value jsonRespBody;
		resp = requestLicense(jsonReqHeader, jsonReqBody, true);

		if (resp.errCode != 0)
		{
			LOGD("error:%d", resp.errCode);
			break;
		}

		if (!oReader.parse(resp.mStrBody, jsonRespBody, false))
		{
			LOGD("error: body json parse error");
			resp.errCode = -1;
			break;
		}

		//
		equipmentInfo["uid"] = jsonRespBody["uid"].asString();
		jsonRespBody["appid"] = appid;
		equipmentInfo["data"] = jsonRespBody;

		licenseOk = true;

	}while (false);

	//离在线授权成功, 则均保存一次，实现双备份逻辑
	if (licenseOk)
	{
		resp.mStrBody.assign(equipmentInfo.toStyledString());

		for(int i = 0; i < stEquipmentInfoPaths.size(); ++i)
		{
        	std::string path = stEquipmentInfoPaths[i];
        	writeLocalLicense(path, equipmentInfo, sPassword);
		}
	}

    return resp;
}

LicenseResp_t doLogin(LicenseReq_t& req)
{
	LicenseResp_t resp;
	LOGD("doLogin");

	::Json::Value jsonReqBody;
	::Json::Reader oReader;
	if (!oReader.parse(req.mBodyJson, jsonReqBody, false)) {

	}

	::Json::Value jsonReqHeader;
	if (!oReader.parse(req.mHeaderJson, jsonReqHeader, false)) {

	}

	::Json::Value& equipmentInfo = g_EquipmentInfo.jsonEquipmentInfo;

	::Json::Value jsonRespBody;

	jsonReqBody["hash"] = equipmentInfo["data"]["hash"];//
	jsonReqHeader["COMP_UID"] = g_EquipmentInfo.jsonEquipmentInfo["uid"].asString();

	resp = requestLicense(jsonReqHeader, jsonReqBody, false);

    resp.type = 2;
    return resp;
}

LicenseResp_t requestLicense(const ::Json::Value& jsonReqHeader, const ::Json::Value& jsonReqBody, bool isLicenseReq)
{
	LicenseResp_t resp;

	CURL *curl = NULL;

	CURLcode res;

	curl = curl_easy_init();

	do {

		if (curl == NULL)
		{
			resp.errCode = -1;
			break;
		}

		struct curl_slist *headers = NULL;

		::Json::Value::Members members = jsonReqHeader.getMemberNames();
		size_t member_size = members.size();
		for (int i = 0; i < member_size; ++i)
		{
			std::string header_item;
			header_item.append(members[i].c_str());
			header_item.append(":");
			header_item.append(jsonReqHeader[members[i]].asString().c_str());
			headers = curl_slist_append(headers, header_item.c_str());
			//LOGD("%s\n", header_item.c_str());
		}

		if (isLicenseReq)
		{
			curl_easy_setopt(curl, CURLOPT_URL, "https://abroad-light.txzing.com/light/service/Register");
		}
		else
		{
			curl_easy_setopt(curl, CURLOPT_URL, "https://abroad-light.txzing.com/light/service/Login");
		}

		curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
		curl_easy_setopt(curl, CURLOPT_POST, 1);//设置为非0表示本次操作为POST

		std: string jsonParam;
		std::string masterPassword = md5(jsonReqHeader["COMP-APPID"].asString() + jsonReqHeader["COMP-TIME"].asString());
		masterPassword = md5(masterPassword + "txz" + jsonReqBody["token"].asString());

		encrpyt(jsonReqBody.toStyledString(), jsonParam, masterPassword);

		//设置要POST的JSON数据
		curl_easy_setopt(curl, CURLOPT_POSTFIELDS, jsonParam.c_str());
		curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, jsonParam.size());//设置上传json串长度,这个设置可以忽略

		std::string respBody;
		std::string respHeader;
		respBody.clear();

		curl_easy_setopt(curl, CURLOPT_WRITEDATA, &respBody);
		curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, my_write_func);
		curl_easy_setopt(curl, CURLOPT_HEADERDATA, &respHeader);
		curl_easy_setopt(curl, CURLOPT_HEADERFUNCTION, my_write_func);
		curl_easy_setopt(curl, CURLOPT_NOPROGRESS, 0L);
		curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, false);
		curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, false);

		res = curl_easy_perform(curl);

		if (res  != CURLE_OK)
		{
			resp.errCode = -1;
			LOGE("curl_easy_perform error");
			break;
		}

		//获取响应码
		long responseCode = -1;
		res = curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &responseCode);

		if (responseCode != 200)
		{
			resp.errCode = -1;
			LOGD("responseCode:%ld", responseCode);
		}

		//获取错误码
		int errCode = 0;
		std::string ssss;
		HEAD_OBJECT header;
		parseHeader(respHeader, &header, "\n");
		getValue("COMP-ERRCODE", &ssss, header);

		if (stringToInt(ssss, &errCode))
		{
			resp.errCode = errCode;
		}

		if (resp.errCode != 0)
		{
			break;
		}

		std::string s;
		decrpyt(respBody, s, masterPassword);

		LOGD("resp body : %s, %s\n", s.c_str(), ssss.c_str());

		resp.mStrBody.assign(s);

	} while (false);

	if (curl != NULL){
		curl_easy_cleanup(curl);
	}

	return resp;
}


