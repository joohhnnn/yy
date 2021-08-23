package com.txznet.audio.server.response;

import com.txznet.comm.remote.util.Logger;
import com.txznet.music.utils.ArrayUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import txz.naming.tele.NamingException;

//import javax.naming.Context;
//import javax.naming.NamingException;
//import javax.naming.directory.Attribute;
//import javax.naming.directory.Attributes;
//import javax.naming.directory.DirContext;
//import javax.naming.directory.InitialDirContext;

public class DNSUtils {



    public static Map<String, Set<String>> mUrlToIPs = new Hashtable<String, Set<String>>(10);


    @SuppressWarnings("rawtypes")
    public static ArrayList<String> getDNSRecs(String domain, String provider, String[] types, int timeout,
                                               int retryCount) /*throws NamingException*/ {

        ArrayList<String> results = new ArrayList<String>(15);

//        Hashtable<String, String> env = new Hashtable<String, String>();
//
//        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
//
//        // 设置域名服务器
//        env.put(Context.PROVIDER_URL, "dns://" + provider);
//
//        // 连接时间
//        env.put("com.sun.jndi.dns.timeout.initial", String.valueOf(timeout));
//
//        // 连接次数
//        env.put("com.sun.jndi.dns.timeout.retries", String.valueOf(retryCount));
//
//        DirContext ictx = new InitialDirContext(env);
//        Attributes attrs = ictx.getAttributes(domain, types);
//
//        for (Enumeration e = attrs.getAll(); e.hasMoreElements(); ) {
//            Attribute a = (Attribute) e.nextElement();
//            int size = a.size();
//            for (int i = 0; i < size; i++) {
//                results.add((String) a.get(i));
//            }
//        }
        return results;
    }

    /**
     * 获取域名所有IP
     *
     * @param domain     域名
     * @param dnsServers DNS服务器列表
     * @param timeout    请求超时
     * @param retryCount 重试次数
     * @return
     */
    public static Set<String> getAllIP(String domain, String[] dnsServers, int timeout, int retryCount) {
        Set<String> ips = new HashSet<String>();

//        for (String dnsServer : dnsServers) {
//            List<String> ipList;
//            try {
//                ipList = getDNSRecs(domain, dnsServer, new String[]{"A"}, timeout, retryCount);
//            } catch (NamingException e) {
//                continue;
//            }
//            ips.addAll(ipList);
//        }

        return ips;
    }

    /**
     * 你要获取域名服务器的ip的第几个
     *
     * @param domain
     * @param i
     * @return
     */
    public static String getIP(String domain, int i) {
        Set<String> ips = null;
//        if (mUrlToIPs.containsKey(domain)) {
//            ips = mUrlToIPs.get(domain);
//        } else {
//            //获取系统的
//            String property = System.getProperty("net.dns1");
//            String property1 = System.getProperty("net.dns2");
//
//            List<String> strings = new ArrayList<String>();
//            if (StringUtils.isNotEmpty(property)) {
//                strings.add(property);
//            }
//            if (StringUtils.isNotEmpty(property1)) {
//                strings.add(property1);
//            }
//            strings.add("223.5.5.5");
//            strings.add("223.6.6.6");
//            strings.add("8.8.8.8");
//            strings.add("8.8.4.4");
//
//
//            String[] dnsServers = CollectionUtils.toStrings(strings);
//            /* new String[]{property, property1,
//                    "223.5.5.5", "223.6.6.6", // 阿里云
//                    // "180.76.76.76", //百度，会做安全过滤，暂时不考虑
//                    // "114.114.114.114", "114.114.115.115", //114DNS，曾出现广告劫持，暂时不考虑
//                    "8.8.8.8", "8.8.4.4",// google
//            };*/
//            ips = getAllIP(domain, dnsServers, 500, 5);
//            mUrlToIPs.put(domain, ips);
//        }
//
//        if (ips.isEmpty()) {
//            Logger.d("music:dns:", domain + ",dns is error:" + mUrlToIPs.toString());
//            return domain;
//        } else {
//            Iterator<String> iterator = ips.iterator();
//            while (iterator.hasNext()) {
//                String next = iterator.next();
//                if (next != null) {
//                    return next;
//                }
//            }
//        }

        //随机取一个，然后放在缓存里面，然后每次先从缓存中获取
        return null;
    }


}
