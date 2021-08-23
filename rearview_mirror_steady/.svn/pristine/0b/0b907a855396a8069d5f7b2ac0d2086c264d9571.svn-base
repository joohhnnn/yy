#ifndef __TXZ_OSAL_LOG_H__
#define __TXZ_OSAL_LOG_H__

#define __STDC_FORMAT_MACROS
#include <inttypes.h>
#include <string.h>
#include <string>
#include<android/log.h>
#include <stdint.h>

enum
{
    OSAL_LOG_DEBUG = ANDROID_LOG_DEBUG,
    OSAL_LOG_INFO = ANDROID_LOG_INFO,
    OSAL_LOG_WARN = ANDROID_LOG_WARN,
    OSAL_LOG_ERROR = ANDROID_LOG_ERROR,
    OSAL_LOG_FATAL = ANDROID_LOG_FATAL
};

#define OSAL_JOIN2(_a, _b) _a##_b
#define OSAL_JOIN1(_a, _b) OSAL_JOIN2(_a, _b)
#define OSAL_JOIN(_a, _b) OSAL_JOIN1(_a, _b)

#define OSAL_LOG_LEVEL_ENABLED(_level) true//osal_log_need_log(_level)

#define OSAL_LOG(_module, _level, _tag, _format, ...) __android_log_print(_level, _tag, _format, ##__VA_ARGS__)
#define OSAL_VLOG(_module, _level, _tag, _format, _arglist) __android_log_vprint(_level, _tag, _format, _arglist)


#define OSAL_LOG_MACRO(_level, _format, ...) \
do \
{ \
    if (OSAL_LOG_LEVEL_ENABLED(_level)) \
    { \
        int OSAL_JOIN(__log_file_name_pos, __LINE__)  = ((int)(std::string(__FILE__).rfind("/"))) + 1; \
        ::std::string OSAL_JOIN(__log_tag, __LINE__) = __PRETTY_FUNCTION__; \
        ::std::string::size_type OSAL_JOIN(__log_tag_start, __LINE__) = ::std::string::npos; \
        ::std::string::size_type OSAL_JOIN(__log_tag_end, __LINE__) =OSAL_JOIN(__log_tag, __LINE__).find_last_of('('); \
        if (::std::string::npos != OSAL_JOIN(__log_tag_end, __LINE__)) \
            OSAL_JOIN(__log_tag, __LINE__).erase(OSAL_JOIN(__log_tag_end, __LINE__)); \
        OSAL_JOIN(__log_tag_start, __LINE__) = OSAL_JOIN(__log_tag, __LINE__).find_last_of(' '); \
        if (::std::string::npos != OSAL_JOIN(__log_tag_start, __LINE__)) \
            OSAL_JOIN(__log_tag, __LINE__).erase(0, OSAL_JOIN(__log_tag_start, __LINE__)+1); \
        OSAL_LOG("",_level, ("TXZ::"+ OSAL_JOIN(__log_tag, __LINE__)).c_str(), _format"[%s:%d]", ##__VA_ARGS__, __FILE__+OSAL_JOIN(__log_file_name_pos, __LINE__), __LINE__); \
    } \
} while(0)

//等级日志宏
#define LOGD(_format, ...) OSAL_LOG_MACRO(OSAL_LOG_DEBUG, _format, ##__VA_ARGS__)
#define LOGI(_format, ...) OSAL_LOG_MACRO(OSAL_LOG_INFO, _format, ##__VA_ARGS__)
#define LOGW(_format, ...) OSAL_LOG_MACRO(OSAL_LOG_WARN, _format, ##__VA_ARGS__)
#define LOGE(_format, ...) OSAL_LOG_MACRO(OSAL_LOG_ERROR, _format, ##__VA_ARGS__)
#define LOGF(_format, ...) OSAL_LOG_MACRO(OSAL_LOG_FATAL, _format, ##__VA_ARGS__)


#endif
