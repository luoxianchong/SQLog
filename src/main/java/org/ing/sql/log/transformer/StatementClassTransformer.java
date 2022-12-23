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
import java.util.function.Function;

/**
 *  字节码transformer
 *
 * @author ing
 * @since 2021/5/12
 */
public abstract class StatementClassTransformer implements ClassFileTransformer {
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

    protected static final ClassPool pool = ClassPool.getDefault();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace("/", ".");
        if ("com.mysql.jdbc.PreparedStatement".equals(className)
            || "com.mysql.cj.jdbc.ClientPreparedStatement".equals(className)
        ) {
            try {
                CtClass ctClass = pool.get(className);
                for (CtMethod m : ctClass.getDeclaredMethods()) {
                    if ("executeInternal".equals(m.getName())) {
                        newMethod(m,this::genCodeSource);
                    }
                }
                return ctClass.toBytecode();
            } catch (NotFoundException | CannotCompileException | IOException e) {
                e.printStackTrace();
            }
        }
        if("com.mysql.jdbc.StatementImpl".equals(className)
                ||"com.mysql.cj.jdbc.StatementImpl".equals(className)
        ){
            try {
                CtClass ctClass = pool.get(className);
                for (CtMethod m : ctClass.getDeclaredMethods()) {
                    if ("executeQuery".equals(m.getName())) {
                        newMethod(m,this::buildQueryAdvice);
                    }
                }
                return ctClass.toBytecode();
            } catch (NotFoundException | CannotCompileException | IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    protected CtMethod newMethod(CtMethod m, Function<Boolean,String> function) throws CannotCompileException, NotFoundException {
        CtMethod copy = CtNewMethod.copy(m, m.getDeclaringClass(), null);
        copy.setName(m.getName() + "$agent");
        m.getDeclaringClass().addMethod(copy);
        if (m.getReturnType().equals(CtClass.voidType)) {
            m.setBody(String.format(function.apply(true), m.getName()));
        } else {
            m.setBody(String.format(function.apply(false), m.getName()));
        }
        return copy;
    }

    public abstract String genCodeSource(boolean returnVoid) ;

    private String buildQueryAdvice(boolean returnVoid){
        return returnVoid?voidCodeSource:codeSource;
    }
}
