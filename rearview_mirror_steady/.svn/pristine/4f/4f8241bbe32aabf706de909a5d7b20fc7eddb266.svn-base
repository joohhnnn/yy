#ifndef __TXZ_LICENSE_INTERFACE_H__
#define __TXZ_LICENSE_INTERFACE_H__

#include <string>
using namespace std;

#ifdef __cplusplus
extern "C"
{
#endif

/*
 * @function:激活回调接口
 * @param:errCode等于0,表示成功，此时msg携带的是激活信息；errCode不等于0表示激活失败，此时errCode表示具体的错误类型，msg携带错误描述信息
 */
typedef void(*OnLicense)(int errCode, const std::string& msg);

void txz_license(const std::string& headerJson, const std::string& bodyJson, OnLicense ptr);

#ifdef __cplusplus
}
#endif

#endif

