#ifndef __TXZ_FILE_UTIL_H__
#define __TXZ_FILE_UTIL_H__

#include<stdio.h>

class FileUtil
{
public:
    //连接路径
    static inline ::std::string joinPath(
            const ::std::string& strPath,
            const ::std::string& strName)
    {
        if (strPath.empty()) return strName;
        if (*strPath.rbegin() == '/' || *strPath.rbegin() == '\\') return strPath + strName;
        return strPath + "/" + strName;
    }

    //获取目录
    static inline ::std::string getDirectory(
            const ::std::string& strPath)
    {
        size_t n = strPath.find_last_of("\\/");
        if (n == ::std::string::npos)
        {
            return ".";
        }
        return strPath.substr(0, n);
    }

    static long getFileSize(
            const ::std::string &strFileName,
            bool bLog = true);

    static inline bool isExist(
            const ::std::string &strFileName)
    {
        return access(strFileName.c_str(), F_OK) == 0;
    }

    //基础读写接口
    static bool saveFile(
            const ::std::string &strFileName,
            const void* pData,
            size_t nLen);
    static bool saveFile(
            const ::std::string &strFileName,
            const ::std::string &strData);
    static bool readFile(
            const ::std::string &strFileName,
            ::std::string &strData);

    //附加数据
    static bool appendFile(
            const ::std::string &strFileName,
            const void* pData,
            size_t nLen);
    static bool appendFile(
            const ::std::string &strFileName,
            const ::std::string &strData);
};

#endif
