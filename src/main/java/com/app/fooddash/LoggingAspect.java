package com.app.fooddash;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(com.app.fooddash.controller..*)")
    public void controllerLayer() {}

    @Around("controllerLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String traceId = MDC.get("traceId");

        Object[] args = joinPoint.getArgs();

        // 🔹 ENTRY (DEBUG → dev only)
        log.debug("ENTER {}.{} traceId={} args={}",
                className,
                methodName,
                traceId,
                sanitizeArgs(args)
        );

        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Exception ex) {

            log.error("ERROR {}.{} traceId={} message={}",
                    className,
                    methodName,
                    traceId,
                    ex.getMessage(),
                    ex
            );

            throw ex;
        }

        long executionTime = System.currentTimeMillis() - startTime;

        // 🔹 EXIT (INFO → always visible)
        log.info("EXIT {}.{} status=success time={}ms traceId={}",
                className,
                methodName,
                executionTime,
                traceId
        );
        return result;
    }

    private Object[] sanitizeArgs(Object[] args) {
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return null;

                    String str = arg.toString().toLowerCase();

                    if (str.contains("password") || str.contains("token")) {
                        return "*****";
                    }

                    return arg;
                })
                .toArray();
    }
}