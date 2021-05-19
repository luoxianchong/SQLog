/*
 * datetest.java
 * Copyright 2021 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package org.ing;

/**
 * Function: 
 *
 * @author zhongming
 * @since 2021/5/12
 */
public class DateTest {

    public static void main(String[] args) {
        String nowTime=java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(java.time.LocalDateTime.now());
        System.out.println(nowTime);
    }
}
