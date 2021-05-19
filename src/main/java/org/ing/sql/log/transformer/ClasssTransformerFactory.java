/*
 * TransformerFactory.java
 * Copyright 2021 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package org.ing.sql.log.transformer;

import java.lang.instrument.ClassFileTransformer;

/**
 * Function: 字节码transformer工厂类
 *
 * @author ing
 * @since 2021/5/12
 */
public class ClasssTransformerFactory {

   public static ClassFileTransformer getTransformer(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return new ClientStatementClassTransformer();
        }catch (Exception e){
            return new PrepareStatementClassTransformer();
        }

    }
}
