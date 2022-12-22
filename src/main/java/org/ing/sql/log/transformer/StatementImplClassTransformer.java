/*
 * StatementClassTransformer.java
 * Copyright 2021 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package org.ing.sql.log.transformer;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 *  字节码transformer
 *
 * @author ing
 * @since 2021/5/12
 */
public class StatementImplClassTransformer extends StatementClassTransformer  {
    protected static final String dateTimeFormat="String nowTime=new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss.SSS\").format(new java.util.Date());\n";

    protected static String  returnPrint="if(r instanceof com.mysql.jdbc.ResultSetInternalMethods ) {\n" +
                                         "            try {\n" +
                                         "                    effect=((com.mysql.jdbc.ResultSetInternalMethods) r).getUpdateCount();\n" +
                                         "            } catch (Exception e) {\n" +
                                         "                e.printStackTrace();\n" +
                                         "            }\n" +
                                         "        }";

    static final String codeSource ="{" +
            "long s=System.currentTimeMillis();" +
            "Object r;\n" +
            "long effect=-1;\n" +
            "try{\n" +
            "r=($w)%s$agent($$);\n" +
            "}finally{\n" +
            dateTimeFormat+
            returnPrint+
            "System.out.println(nowTime+\"-elapse:[\"+(System.currentTimeMillis()-s)+\"ms]-sql:[\"+$1+\"]-effect rows:[\"+effect+\"]\");\n" +
            "}\n" +
            "return ($r)r;" +
            "}";

    static final String voidCodeSource = "{" +
            "long s=System.currentTimeMillis();" +
            "try{\n" +
            "%s$agent($$);\n" +
            "}finally{\n" +
            dateTimeFormat+
            "System.out.println(nowTime+\"-elapse:[\"+(System.currentTimeMillis()-s)+\"ms]-sql:[\"+$1+\"]\");\n" +
            "}\n" +
            "}";

    @Override
    public String genCodeSource(boolean returnVoid) {
        return returnVoid?voidCodeSource:codeSource;
    }
}
