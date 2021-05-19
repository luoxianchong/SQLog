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
public class StatementClassTransformer implements ClassFileTransformer {
    protected static final String dateTimeFormat="String nowTime=new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss.SSS\").format(new java.util.Date());\n";

    protected static String returnPrint="if(r instanceof java.sql.ResultSet){\n try { effect = ((java.sql.ResultSet) r).getFetchSize(); }catch (Exception e){ e.printStackTrace(); }\n  }";

    static final String codeSource ="{" +
            "long s=System.currentTimeMillis();" +
            "Object r;\n" +
            "long effect=-1;\n" +
            "try{\n" +
            "r=($w)%s$agent($$);\n" +
            "}finally{\n" +
            dateTimeFormat+
            returnPrint+
            "System.out.println(nowTime+\"-elapse:[\"+(System.currentTimeMillis()-s)+\"ms]-sql:[\"+$0.asSql()+\"]-effect rows:[\"+effect+\"]\");\n" +
            "}\n" +
            "return ($r)r;" +
            "}";

    static final String voidCodeSource = "{" +
            "long s=System.currentTimeMillis();" +
            "try{\n" +
            "%s$agent($$);\n" +
            "}finally{\n" +
            dateTimeFormat+
            "System.out.println(nowTime+\"-elapse:[\"+(System.currentTimeMillis()-s)+\"ms]-sql:[\"+$0.asSql()+\"]\");\n" +
            "}\n" +
            "}";

    protected final static ClassPool pool = ClassPool.getDefault();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace("/", ".");
        if ("com.mysql.jdbc.PreparedStatement".equals(className) || "com.mysql.cj.jdbc.ClientPreparedStatement".equals(className)) {
            try {
                CtClass ctClass = pool.get(className);
                for (CtMethod m : ctClass.getDeclaredMethods()) {
                    if (m.getName().equals("executeInternal")) {
                        newMethod(m);
                    }
                }
                return ctClass.toBytecode();
            } catch (NotFoundException | CannotCompileException | IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected CtMethod newMethod(CtMethod m) throws CannotCompileException, NotFoundException {
        CtMethod copy = CtNewMethod.copy(m, m.getDeclaringClass(), null);
        copy.setName(m.getName() + "$agent");
        m.getDeclaringClass().addMethod(copy);
        if (m.getReturnType().equals(CtClass.voidType)) {
            m.setBody(String.format(genCodeSource(true), m.getName()));
        } else {
            m.setBody(String.format(genCodeSource(false), m.getName()));
        }
        return copy;
    }

    public String genCodeSource(boolean returnVoid) {
        return returnVoid?voidCodeSource:codeSource;
    }
}
