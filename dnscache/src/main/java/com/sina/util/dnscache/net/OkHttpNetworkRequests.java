/**
 * 
 */
package com.sina.util.dnscache.net;

import com.sina.util.dnscache.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 
 * 项目名称: DNSCache 类名称: OkHttpNetworkRequests
 * 类描述: 轻量级的网络请求库 
 * 创建人: lizhangqu
 * 使用 Apache HttpClient 实现网络请求 
 * 创建时间: 2016-1-27 上午11:50:49
 * 
 * 修改人: lizhangqu
 * 修改时间:  2016-1-27
 * 修改备注: HTTPS不进行域名校验
 * 
 * @version V2.0
 */
public class OkHttpNetworkRequests implements INetworkRequests {
    private static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;
    private static final int CONNECTION_TIMEOUT = 30 * 1000;
    private static final int SOCKET_BUFFER_SIZE = 8192;

    public String requests(String url) {
        return requests(url, "");
    }

    public static OkHttpClient newInstance() {
        OkHttpClient.Builder builder=new OkHttpClient.Builder();
        // 不自动处理重定向请求
        builder.followRedirects(false);
        builder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        builder.readTimeout(SOCKET_OPERATION_TIMEOUT,TimeUnit.MICROSECONDS);
        builder.writeTimeout(SOCKET_OPERATION_TIMEOUT,TimeUnit.MICROSECONDS);
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

       //builder.sslSocketFactory()


        return builder.build();
    }

    @Override
    public String requests(String url, String host) {
        HashMap<String, String> map = null;
        if (host == null || host.equals("")) {
            map = null;
        } else {
            map = new HashMap<String, String>();
            map.put("host", host);
        }

        return requests(url, map);
    }

    @Override
    public String requests(String url, HashMap<String, String> head) {

        String result = null;

        try {
            OkHttpClient client = newInstance();

            Request.Builder builder=new Request.Builder();


            if (head != null) {
                for (Entry<String, String> entry : head.entrySet()) {
                    Tools.log("TAG", "" + entry.getKey() + "  -  " + entry.getValue());
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            builder.url(url);

            Request request = builder.build();

            Response response = client.newCall(request).execute();

            result=response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public byte[] requestsByteArr(String url, HashMap<String, String> head) {

        byte[] result = null;

        try {
            OkHttpClient client = newInstance();
            Request.Builder builder=new Request.Builder();

            if (head != null) {
                for (Entry<String, String> entry : head.entrySet()) {
                    Tools.log("TAG", "" + entry.getKey() + "  -  " + entry.getValue());
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            builder.url(url);
            Request request = builder.build();
            Response response = client.newCall(request).execute();

            result = response.body().bytes();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return result;
    }



    public static boolean upLoadFile(String url, File file) {
        boolean result = false;
        try {
            if (null == url || null == file || !file.exists() || file.length() < 1) {
                return false;
            }
            /**
             * 第一部分
             */
            URL urlObj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

            /**
             * 设置关键值
             */
            con.setRequestMethod("POST"); // 以Post方式提交表单，默认get方式
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false); // post方式不能使用缓存

            // 设置请求头信息
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");

            // 设置边界
            String BOUNDARY = "----------" + System.currentTimeMillis();
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

            // 请求正文信息

            // 第一部分：
            StringBuilder sb = new StringBuilder();
            sb.append("--"); // ////////必须多两道线
            sb.append(BOUNDARY);
            sb.append("\r\n");
            sb.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
            sb.append("Content-Type:application/octet-stream\r\n\r\n");

            byte[] head = sb.toString().getBytes("utf-8");

            // 获得输出流
            OutputStream out = new DataOutputStream(con.getOutputStream());
            out.write(head);

            // 文件正文部分
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            int bytes = 0;
            byte[] bufferOut = new byte[1024];
            while ((bytes = in.read(bufferOut)) != -1) {
                out.write(bufferOut, 0, bytes);
            }
            in.close();

            // 结尾部分
            byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线
            out.write(foot);
            out.flush();
            out.close();

            int statusCode = con.getResponseCode();
            if (statusCode == 200) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
