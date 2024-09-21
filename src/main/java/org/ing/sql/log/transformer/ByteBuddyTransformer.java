package org.ing.sql.log.transformer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhongming
 * @since 2022/12/23
 */
public class ByteBuddyTransformer implements AgentBuilder.Transformer,AgentBuilder.Listener {

    private static final Logger log = LoggerFactory.getLogger(ByteBuddyTransformer.class);


    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDesc, ClassLoader cl, JavaModule module) {
        if ( "PreparedStatement".equals(typeDesc.getSimpleName())||"ClientPreparedStatement".equals(typeDesc.getSimpleName())) {
            // 委托
            return builder.method(ElementMatchers.named("executeInternal").and(ElementMatchers.isProtected()))
                          .intercept(MethodDelegation.to(SQLInterceptor.class));

        }else if("StatementImpl".equals(typeDesc.getSimpleName())){
            return builder.method(ElementMatchers.named("executeQuery").and(ElementMatchers.isPublic()))
                          .intercept(MethodDelegation.to(SQLInterceptor.class));
        }
        return builder;
    }

    @Override
    public void onDiscovery(String typeName, ClassLoader cl, JavaModule module, boolean b) {
        if (typeName.startsWith("com.mysql")) {
            System.out.println("--- onDiscovery ---" + typeName);
            log.info("--- onDiscovery ---" + typeName);
        }
    }

    @Override
    public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
        if (typeDescription.getSimpleName().startsWith("com.mysql")) {
            System.out.println("--- onTransformation ---" + typeDescription.getSimpleName());
            log.info("--- onTransformation ---" + typeDescription.getSimpleName());
        }
    }

    @Override
    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {

    }

    @Override
    public void onError(String typeName, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {
        if (typeName.startsWith("com.mysql")) {
            System.out.println("--- onError ---" + throwable);
        }
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule javaModule, boolean b) {
        if (typeName.startsWith("com.mysql")) {
            System.out.println("--- onComplete ---" + typeName);
            log.info("--- onComplete ---" + typeName);
        }
    }




}
