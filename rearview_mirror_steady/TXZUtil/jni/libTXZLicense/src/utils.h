#ifndef __TXZ_UTILS_H__
#define  __TXZ_UTILS_H__

#include <string>
#include <stdlib.h>
#include <iostream>
#include <string>
#include <map>
#include <algorithm>
#include<cstdlib>
#include<stdlib.h>

using namespace std;

#define LOG(lvl, fmt, args...) \
 do{\
  __android_log_print(lvl,"TXZ" , "[%ld][%d]" fmt "[%s:%d][%s]", (long)time(NULL), (int)gettid(), ##args, __FILE__, __LINE__, __FUNCTION__);\
 }while(0)

#define LOGD(fmt, args...) LOG(ANDROID_LOG_DEBUG , fmt, ##args)
#define LOGI(fmt, args...) LOG(ANDROID_LOG_INFO , fmt, ##args)
#define LOGW(fmt, args...) LOG(ANDROID_LOG_WARN , fmt, ##args)
#define LOGE(fmt, args...) LOG(ANDROID_LOG_ERROR , fmt, ##args)
#define LOGF(fmt, args...) LOG(ANDROID_LOG_FATAL , fmt, ##args)

bool encrpyt(const std::string& in, std::string& out, const std::string& key);

bool decrpyt(const std::string& in, std::string& out, const std::string& key);

void ByteToHexStr(const unsigned char* source, char* dest, int sourceLen);
char *padding_buf(const char *buf,int size, int *final_size);
void encrpyt_buf(const char * in, int in_size, char * out,  const char* key);
std::string md5(const std::string& orig);

typedef struct HEAD_OBJECT_S {
	std::string version;
	std::string statusCode;
	std::string status;
	std::map<string,string> value;
} HEAD_OBJECT;

bool parseHeader(const std::string& sHead, HEAD_OBJECT* headValue, const std::string endSymbol);
void trim(string *s);
bool getValue(const std::string key, std::string *value, const HEAD_OBJECT head);
bool stringToInt(const std::string str, int* val);

#endif

