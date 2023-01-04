/*
 * SQLAgent.java
 * Copyright 2021 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package org.ing.sql.log;

import java.lang.instrument.Instrumentation;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import org.ing.sql.log.transformer.ByteBuddyTransformer;
import  net.bytebuddy.matcher.ElementMatchers;

/**
 * sql 日志 agent
 *
 * @author ing
 * @since 2021/5/11
 */
public class SQLAgent {

    public static void premain(String arg, Instrumentation instrumentation) {
        System.out.println("===========================> premain--SQLogAgent <===============================");
        byteProxy().installOn(instrumentation);
    }


    public static void agentmain(String arg, Instrumentation instrumentation) {
        System.out.println("===========================> agentmain--SQLogAgent <===============================");
        byteProxy().installOn(instrumentation);
    }

    private static AgentBuilder byteProxy(){
        ByteBuddyTransformer transformer = new ByteBuddyTransformer();
        final ByteBuddy byteBuddy = new ByteBuddy().with(TypeValidation.of(false));
       return new AgentBuilder.Default(byteBuddy)
                .type(ElementMatchers.namedOneOf("com.mysql.jdbc.PreparedStatement","com.mysql.jdbc.StatementImpl",
                        "com.mysql.cj.jdbc.ClientPreparedStatement","com.mysql.cj.jdbc.StatementImpl"))
                //.with((AgentBuilder.Listener) transformer)
                // update the byte code
                .transform(transformer);

    }
}

