/*
 * SQLAgent.java
 * Copyright 2021 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package org.ing.sql.log;

import java.lang.instrument.Instrumentation;

import org.ing.sql.log.transformer.ClasssTransformerFactory;
import org.ing.sql.log.transformer.StatementClassTransformer;
import org.ing.sql.log.transformer.StatementImplClassTransformer;

/**
 * sql 日志 agent
 *
 * @author ing
 * @since 2021/5/11
 */
public class SQLAgent {

    public static void premain(String arg, Instrumentation instrumentation) {
        instrumentation.addTransformer(ClasssTransformerFactory.getTransformer());
        instrumentation.addTransformer(new StatementImplClassTransformer());
    }

    public static void agentmain(String arg, Instrumentation instrumentation) {
        instrumentation.addTransformer(ClasssTransformerFactory.getTransformer());
        instrumentation.addTransformer(new StatementImplClassTransformer());
    }
}

