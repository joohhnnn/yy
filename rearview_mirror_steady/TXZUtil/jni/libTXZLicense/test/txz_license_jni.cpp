#include<string>
#include<stdio.h>
#include<jni.h>

#include "txz_license_interface.h"

 using namespace std;

 extern "C" JNIEXPORT void JNICALL Java_com_txznet_txz_LicenseManager_license(JNIEnv *env, jobject obj, jstring headerJson, jstring bodyJson);

 void onEvent(int errCode, const std::string&  msg)
 {
	 //LOGD("%d, %s", errCode, msg.c_str());
 }

JNIEXPORT void JNICALL Java_com_txznet_txz_LicenseManager_license(JNIEnv *env, jobject obj, jstring headerJson, jstring bodyJson)
{

	const char* ptrBuf = NULL;
	jsize bufSize = 0;

	std::string sHeaderJson;
	std::string sBodyJson;

	ptrBuf = env->GetStringUTFChars(headerJson, 0);
	bufSize = env->GetStringUTFLength(headerJson);
	sHeaderJson.assign(ptrBuf, bufSize);
	env->ReleaseStringUTFChars(headerJson, ptrBuf);

	ptrBuf = env->GetStringUTFChars(bodyJson, 0);
	bufSize = env->GetStringUTFLength(bodyJson);
	sBodyJson.assign(ptrBuf, bufSize);
	env->ReleaseStringUTFChars(bodyJson, ptrBuf);

	txz_license(sHeaderJson, sBodyJson, onEvent);
}







