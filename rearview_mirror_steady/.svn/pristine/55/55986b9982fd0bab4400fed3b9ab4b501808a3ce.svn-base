#include <stdlib.h>
#include <dirent.h>
#include <unistd.h>
#include <string>
#include <sys/stat.h>
#include <sys/types.h>

#include "txz_file_util.h"
#include "txz_license.h"

using namespace std;

size_t OSAL_getFileSize(const std::string& sFile)
{
    FILE *fp = fopen(sFile.c_str(), "rb");
    if (NULL == fp)return 0;
    fseek(fp, 0, SEEK_END);
    long l = ftell(fp);
    fclose(fp);
    return l;
}

bool OSAL_mkdirp(
        const char *dir,
        int mode)
{
    char tmp[512];
    char *p = NULL;
    size_t len;

    snprintf(tmp, sizeof(tmp), "%s", dir);
    len = strlen(tmp);
    if (len == 0) return true;
    if (tmp[len - 1] == '/' || tmp[len - 1] == '\\') tmp[len - 1] = '\0';
    for (p = tmp + 1; *p; p++)
    {
        if (*p == '/' || *p == '\\')
        {
            *p = '\0';
            mkdir(tmp, mode);
            *p = '/';
        }
    }
    int ret = mkdir(tmp, mode);
    if (ret != 0)
    {
        return false;
    }
    return true;
}


bool OSAL_isDir(
        const char *dir)
{
    if (dir == NULL)
    {
        return false;
    }
    struct stat s;
    int err = stat(dir, &s);
    if (-1 != err && S_ISDIR(s.st_mode))
    {
        return true;
    }
    return false;
}

time_t OSAL_getFileModifyTime(
        const std::string& sFile)
{
    struct stat st;
    if (0 != stat(sFile.c_str(), &st))
    {
        return 0;
    }
    return st.st_mtime;
}

time_t OSAL_getFileCreateTime(
        const std::string& sFile)
{
    struct stat st;
    if (0 != stat(sFile.c_str(), &st))
    {
        return 0;
    }
    return st.st_ctime;
}

int OSAL_rmdir(
        const ::std::string& strDir)
{
    DIR* dp = NULL;
    struct dirent* dirp;
    dp = opendir(strDir.c_str());
    if (dp == NULL)
    {
        return -1;
    }

    while ((dirp = readdir(dp)) != NULL)
    {
        if (strcmp(dirp->d_name, "..") == 0 || strcmp(dirp->d_name, ".") == 0) continue;
        ::std::string strFile = strDir + "/" + dirp->d_name;
        if (dirp->d_type == DT_DIR)
        {
            int r = OSAL_rmdir(strFile);
            if (0 != r) break;
        }
        else
        {
            if (0 != remove(strFile.c_str())) break;
        }
    }
    closedir(dp);
    return rmdir(strDir.c_str());
}

int OSAL_cp(
        const ::std::string& strSrcFile,
        const ::std::string& strDstFile)
{
    FILE *fsrc = fopen(strSrcFile.c_str(), "rb");
    FILE *fdst = fopen(strDstFile.c_str(), "wb");
    int ret = 0;
    do
    {
        if (NULL == fsrc)
        {
            ret = -1;
            break;
        }
        if (NULL == fdst)
        {
            ret = -2;
            break;
        }

        char buf[1024] = { 0 };
        do
        {
            int r = fread(buf, 1, sizeof(buf), fsrc);
            if (r < 0)
            {
                ret = r;
                break;
            }
            if ((int) (fwrite(buf, 1, r, fdst)) != r)
            {
                ret = -3;
                break;
            }
            if (r < (int) sizeof(buf)) break;
        } while (0);
    } while (0);
    if (NULL != fsrc) fclose(fsrc);
    if (NULL != fdst) fclose(fdst);
    return ret;
}

int OSAL_cpdir(
        const ::std::string& strSrcDir,
        const ::std::string& strDstDir)
{
    OSAL_rmdir(strDstDir);

    int ret = 0;

    DIR * dir;
    struct dirent *dirp;

    dir = opendir(strSrcDir.c_str());
    if (NULL == dir) return -1;

    if (!OSAL_mkdirp(strDstDir.c_str(), 744)) return -2;

    while ((dirp = readdir(dir)) != NULL)
    {
        if (strcmp(dirp->d_name, "..") == 0 || strcmp(dirp->d_name, ".") == 0) continue;
        ::std::string strSrcFile = strSrcDir + "/" + dirp->d_name;
        ::std::string strDstFile = strDstDir + "/" + dirp->d_name;
        if (dirp->d_type == DT_DIR)
        {
            if (0 != OSAL_cpdir(strSrcFile, strDstFile))
            {
                ret = -3;
                break;
            }
        }
        else
        {
            if (0 != OSAL_cp(strSrcFile, strDstFile))
            {
                ret = -4;
                break;
            }
        }
    }
    closedir(dir);
    return ret;
}


long FileUtil::getFileSize(
        const ::std::string &strFileName,
        bool bLog)
{
    struct stat the_stat;
    if (stat(strFileName.c_str(), &the_stat) < 0)
    {
        //if (bLog) LOGE("stat [%s] fail,err:%d, %s", strFileName.c_str(), errno, strerror(errno));
        return -1;
    }
    return (long) the_stat.st_size;
}

bool FileUtil::saveFile(
        const ::std::string &strFileName,
        const void* pData,
        size_t nLen)
{
    OSAL_mkdirp(getDirectory(strFileName).c_str(), 0774);
    bool ret = false;
    FILE *f = fopen(strFileName.c_str(), "wb");
    do
    {
        if (f == NULL)
        {
            //LOGE("open [%s] fail,err:%d-%s", strFileName.c_str(), errno, strerror(errno));
            break;
        }
        int wr = (int) fwrite(pData, 1, nLen, f);
        if (wr != (signed) nLen)
        {
            //LOGE("write [%s] fail [%d],err:%d-%s", strFileName.c_str(), wr, errno, strerror(errno));
            break;
        }
        fflush(f);
        ret = true;
    } while (0);

    if (NULL != f) fclose(f);

    return ret;
}

bool FileUtil::saveFile(
        const ::std::string &strFileName,
        const ::std::string &strData)
{
    return saveFile(strFileName, strData.data(), strData.size());
}

bool FileUtil::readFile(
        const ::std::string &strFileName,
        ::std::string &strData)
{
    bool ret = false;
    FILE *f = fopen(strFileName.c_str(), "rb");

    do
    {
        if (f == NULL)
        {
//            if (errno != ENOENT)
//            {
//                LOGE("open [%s] fail,err:%d-%s", strFileName.c_str(), errno, strerror(errno));
//            }

            break;
        }
        if (0 != fseek(f, 0L, SEEK_END))
        {
            //LOGE("seek [%s] fail,err:%d, %s", strFileName.c_str(), errno, strerror(errno));
            break;
        }
        int sz = (int) ftell(f);
        if (sz < 0)
        {
            //LOGE("get [%s] size fail[%d],err:%d-%s", strFileName.c_str(), sz, errno, strerror(errno));
            break;
        }
        if (sz == 0)
        {
            ret = true;
            break;
        }
        fseek(f, 0L, SEEK_SET);
        strData.resize(sz);
        sz = (int) fread(&(strData[0]), 1, sz, f);
        if (sz != (signed) strData.size())
        {
            //LOGE("read [%s] fail [%d],err:%d-%s", strFileName.c_str(), sz, errno, strerror(errno));
            break;
        }
        ret = true;
    } while (0);
    if (NULL != f) fclose(f);

    return ret;
}

