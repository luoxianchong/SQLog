/*
 * PrepareStatementClassTransformer.java
 * Copyright 2021 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package org.ing.sql.log.transformer;

/**
 *  PreparedStatment字节码转换器
 *
 * @author ing
 * @since 2021/5/12
 */
public class PrepareStatementClassTransformer extends StatementClassTransformer {

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

    @Override
    public String genCodeSource(boolean returnVoid) {
        return returnVoid?voidCodeSource:codeSource;
    }

}
