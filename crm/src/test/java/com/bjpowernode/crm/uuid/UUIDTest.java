package com.bjpowernode.crm.uuid;

import java.util.UUID;

/**
 * @author 冠军
 * @version 1.0
 */
public class UUIDTest {
    public static void main(String[] args) {
        String uuid = UUID.randomUUID().toString().replace("-","");
        System.out.println(uuid);
    }
}
