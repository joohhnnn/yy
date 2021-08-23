#include "utils.h"
#include "aes.h"
#include <aes.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <stdlib.h>
#include <md5.h>
#include <pem.h>
#include <bio.h>
#include <evp.h>
#include <stdlib.h>
#include <iostream>
#include <string>
#include <map>
#include <algorithm>
#include<cstdlib>
#include<stdlib.h>

#include "txz_license.h"

#define MAXDATABUFF (16)
void ByteToHexStr(const unsigned char* source, char* dest, int sourceLen);
void decrpyt_buf(const char *in , int in_size, char *out, const char* key);
void hex(const string& in, string& out);

void print_byte_as_hex(const char* buffer, int lenght)
{
	std::string s;
	std::string tmp;
	tmp.assign(buffer, lenght);
	hex(tmp, s);
	LOGD("0:%s", s.c_str());
}

void print_str_as_hex(const std::string& strByte)
{
	print_byte_as_hex(strByte.data(), strByte.size());
}

string&   replace_all(string&   str,const   string&   old_value,const   string&   new_value)
   {
       while(true)   {
           string::size_type   pos(0);
           if(   (pos=str.find(old_value))!=string::npos   )
               str.replace(pos,old_value.length(),new_value);
           else   break;
       }
       return   str;
   }
   string&   replace_all_distinct(string&   str,const   string&   old_value,const   string&   new_value)
   {
       for(string::size_type   pos(0);   pos!=string::npos;   pos+=new_value.length())   {
           if(   (pos=str.find(old_value,pos))!=string::npos   )
               str.replace(pos,old_value.length(),new_value);
           else   break;
       }
       return   str;
   }


void hex(const string& in, string& out)
{
	char* ptrOutBuf = (char*)malloc(in.size() * 2);
	ByteToHexStr((const unsigned char*)in.c_str(), ptrOutBuf, in.size());
	out.assign(ptrOutBuf, in.size() * 2);
	free(ptrOutBuf);
}

int base64_encode(const char *in_str, int in_len, char ** out_str)
{

    BIO *b64, *bio;

    BUF_MEM *bptr = NULL;

    size_t size = 0;

    if (in_str == NULL)

        return 0;



    b64 = BIO_new(BIO_f_base64());

    bio = BIO_new(BIO_s_mem());

    bio = BIO_push(b64, bio);



    BIO_write(bio, in_str, in_len);

    BIO_flush(bio);



    BIO_get_mem_ptr(bio, &bptr);

    *out_str = (char*)malloc(bptr->length);
    memcpy(*out_str, bptr->data, bptr->length);
    size = bptr->length;

    BIO_free_all(bio);

    return size;

}



int base64_decode(const char *in_str, int in_len, char ** out_str)

{

    BIO *b64, *bio;

    BUF_MEM *bptr = NULL;

    int counts;

    int size = 0;

    if (in_str == NULL)

        return 0;



    b64 = BIO_new(BIO_f_base64());

    BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);



    bio = BIO_new_mem_buf(in_str, in_len);

    bio = BIO_push(b64, bio);

    *out_str = (char*)malloc(in_len);

    size = BIO_read(bio, *out_str, in_len);

    BIO_free_all(bio);

    return size;

}

bool encrpyt(const std::string& in, std::string& out, const std::string& key)
{
	int padding_in_size = 0;
	char* padding_in = padding_buf(in.c_str(), in.size(), &padding_in_size);
	print_byte_as_hex(padding_in, padding_in_size);
	char* out_buf = (char*)malloc(padding_in_size);
	char* out_buf_base64 = NULL;
	int out_buf_base64_size = 0;
	encrpyt_buf(padding_in, padding_in_size, out_buf, key.c_str());
	out_buf_base64_size = base64_encode(out_buf, padding_in_size, &out_buf_base64);
	out.assign(out_buf_base64, out_buf_base64_size);
	free(padding_in);
	free(out_buf);
	free(out_buf_base64);

	return false;
}

bool decrpyt(const std::string& in, std::string& out, const std::string& key)
{
	//base64->byte
	int base64_decode_size = 0;
	char* base64_decode_buf = NULL;
	std::string ss = in;
	ss = replace_all(ss, "\n", "");//base64逆向的时候，需要去掉\n,不然会逆向失败
	base64_decode_size = base64_decode(ss.c_str(), ss.size(), &base64_decode_buf);
	out.assign(base64_decode_buf, base64_decode_size);
	print_str_as_hex(out);
	free(base64_decode_buf);

	char* out_buf = (char*)malloc(base64_decode_size);
	decrpyt_buf(out.c_str(), out.size(), out_buf, key.c_str());

	//去padding
	int padding_value = (int)out_buf[base64_decode_size - 1];
	LOGD("padding_value:%d", padding_value);

	base64_decode_size = base64_decode_size - padding_value;

	out.assign(out_buf, base64_decode_size);
	print_str_as_hex(out);
	free(out_buf);
	return false;
}

std::string md5(const std::string& orig)
{
	std::string s;
	MD5_CTX md5_ctx;
	MD5_Init(&md5_ctx);
	char DataBuff[MAXDATABUFF] = {0};
	MD5_Update(&md5_ctx, orig.c_str(), orig.size());   //将当前文件块加入并更新MD5
	MD5_Final((unsigned char*)DataBuff, &md5_ctx);//获取MD5

	char hexDataBuff[32] = {0};
	ByteToHexStr((unsigned char*)DataBuff, hexDataBuff, sizeof(DataBuff));
	s.assign(hexDataBuff, sizeof(hexDataBuff));

	return s;
}

void ByteToHexStr(const unsigned char* source, char* dest, int sourceLen)
{
    short i;
    unsigned char highByte, lowByte;


    for (i = 0; i < sourceLen; i++)
    {
        highByte = source[i] >> 4;
        lowByte = source[i] & 0x0f ;

        highByte = highByte > 9 ? 'a' +(highByte - 10):'0' + highByte;
        lowByte = lowByte > 9 ? 'a' +(lowByte - 10):'0' + lowByte;

         dest[i * 2] = highByte;
         dest[i * 2 + 1] = lowByte;
    }
    return ;
}

char *padding_buf(const char *buf, int size, int *final_size) {
        char *ret = NULL;
        int pidding_size = AES_BLOCK_SIZE - (size % AES_BLOCK_SIZE);
        int i = 0;

        *final_size = size + pidding_size;
        ret = (char *)malloc(size+pidding_size);
        memcpy(ret, buf, size);
        if (pidding_size!=0) {
                for (i =size;i < (size+pidding_size); i++ ) {
                        ret[i] = pidding_size;
                }
        }

        return ret;
}

void encrpyt_buf(const char * in, int in_size, char * out,  const char* key)
{
        AES_KEY aes;
        AES_set_encrypt_key((const unsigned char*)key,128, &aes);
        int blk_cnt = in_size/AES_BLOCK_SIZE;

        for(int i = 0; i < blk_cnt; ++i){
        	AES_ecb_encrypt((const unsigned char*)in + i * AES_BLOCK_SIZE, (unsigned char*)out + i * AES_BLOCK_SIZE, &aes, AES_ENCRYPT);
        }
}

void decrpyt_buf(const char *in , int in_size, char *out, const char* key)
{
    AES_KEY aes;
    AES_set_decrypt_key((const unsigned char*)key,128, &aes);
    int blk_cnt = in_size/AES_BLOCK_SIZE;

    for(int i = 0; i < blk_cnt; ++i){
    	AES_ecb_encrypt((const unsigned char*)in + i * AES_BLOCK_SIZE, (unsigned char*)out + i * AES_BLOCK_SIZE, &aes, AES_DECRYPT);
    }
 }

bool parseHeader(const std::string& sHead, HEAD_OBJECT* headValue, const std::string endSymbol) {
	int p = 0;

	string head = "HTTP";
	unsigned int loc = (sHead).find(head, p);
	if( loc == string::npos ){
		return false;
	}
	if (loc != 0) {
		return false;
	}
	p = loc + head.size() + 1; // 去掉/

	loc = (sHead).find(" ", p);
	if( loc == string::npos ){
		return false;
	}
	if (loc < p) {
		return false;
	}
	headValue->version = (sHead).substr(p, loc-p);
	trim(&headValue->version);
	p = loc + 1; //去掉空格

	loc = (sHead).find(" ", p);
	if( loc == string::npos ){
		return false;
	}
	if (loc < p) {
		return false;
	}
	headValue->statusCode = (sHead).substr(p, loc-p);
	trim(&headValue->statusCode);
	p = loc + 1; //去掉空格

	loc = (sHead).find(endSymbol, p);
	if( loc == string::npos ){
		return false;
	}
	if (loc < p) {
		return false;
	}
	headValue->status = (sHead).substr(p, loc-p);
	trim(&headValue->status);
	p = loc + endSymbol.size();

	while (string::npos != (loc = (sHead).find(endSymbol, p)) && (loc < (sHead).size())) {
		string str = (sHead).substr(p, loc - p);
		int split = str.find(":");
		string key = str.substr(0, split);
		transform(key.begin(), key.end(), key.begin(), ::tolower);
		string val = str.substr(split + 1);
		trim(&key);
		trim(&val);
		p = loc + endSymbol.size(); //去掉结束符
		headValue->value[key] = val;
	}

	string str = (sHead).substr(p, loc - p);
	int split = str.find(":");
	string key = str.substr(0, split);
	transform(key.begin(), key.end(), key.begin(), ::tolower);
	string val = str.substr(split + 1);
	trim(&key);
	trim(&val);
	headValue->value[key] = val;

	return true;
}

void trim(string *s){
	int end = 0;
	while((*s)[s->size() - 1 - end] == '\r' || (*s)[s->size() - 1 - end] == '\n')
	{
		end++;
	}
	if (end > 0 && end < s->size())
	{
		s->erase(s->size() - end);
	}
	s->erase(0,s->find_first_not_of(" "));
	s->erase(s->find_last_not_of(" ") + 1);
}

bool getValue(const std::string key, std::string *value, const HEAD_OBJECT head) {
	string headKey = key;
	transform(headKey.begin(), headKey.end(), headKey.begin(), ::tolower);
	*value = (head.value.find(headKey))->second;
	return true;
}

bool stringToInt(const std::string str, int* val) {
	bool ret = false;
	int value = atoi(str.c_str());

	char temp[32] = {0};
	snprintf(temp, sizeof(temp) - 1, "%d", value);
	std::string ss = temp;
	ret = str == ss;
	if (ret)
	{
		*val = value;
	}
	return ret;
}
