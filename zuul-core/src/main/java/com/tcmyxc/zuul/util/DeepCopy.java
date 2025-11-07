package com.tcmyxc.zuul.util;


import java.io.*;

public class DeepCopy {

    public static Object copy(Object src) throws NotSerializableException{
        Object target = null;
        try{
            // 将src写入字节数组
            // 1、构造一个输出流
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            // 2、将对象src序列化并写入ByteArrayOutputStream
            out.writeObject(src);
            out.flush();
            out.close();

            // 创建一个输入流，从字节数组中读取数据
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            // 从输入流中读取对象并反序列化为一个新的对象target
            target = in.readObject();
        }
        catch (NotSerializableException e){
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return target;
    }
}
