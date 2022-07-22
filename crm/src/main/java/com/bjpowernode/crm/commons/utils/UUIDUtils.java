package com.bjpowernode.crm.commons.utils;

import java.util.UUID;

/**
 * @author 冠军
 * @version 1.0
 */
public class UUIDUtils {
    //获取uuid的值
    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
