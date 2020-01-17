package com.wxk.timeserverdemo.util;

import androidx.annotation.NonNull;

import com.google.common.io.BaseEncoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.security.MessageDigest;


/**
 * Author: GIndoc on 2017/4/9 15:57
 * email : 735506583@qq.com
 * FOR   :
 */
public class EnCodeUtil {

    @NonNull
    public static String objectEncode(Object object){
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            // 创建对象输出流，并封装字节流
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            // 将对象写入字节流
            oos.writeObject(object);
        }catch (IOException ex){
            ex.printStackTrace();
        }
        // 将字节流编码成base64的字符窜
        BaseEncoding baseEncoding = BaseEncoding.base64();
        return baseEncoding.encode(baos.toByteArray()); // 将字节以Base64编码
    }

    @NonNull
    public static  <T extends Object> T objectDecode(String encodeString){
        T object = null;
        BaseEncoding baseEncoding = BaseEncoding.base64();
        byte[] base64 = baseEncoding.decode(encodeString);
        ByteArrayInputStream bais = new ByteArrayInputStream(base64);        //封装到字节流
        try {
            //再次封装
            ObjectInputStream bis = new ObjectInputStream(bais);
            try {
                //读取对象
                object = (T) bis.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static String MD5(String key) {
        char hexDigits[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
        try {
            byte[] btInput = key.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }
}

