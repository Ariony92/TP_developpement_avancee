package tp_avancee_dev.tp_avancee.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* tp_avancee_dev.tp_avancee.service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String args = sanitizeArgs(joinPoint.getArgs());

        log.info("[ENTER] {}.{}({})", className, methodName, args);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            log.info("[EXIT]  {}.{} — {}ms — result={}", className, methodName, duration, summarize(result));
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("[ERROR] {}.{} — {}ms — exception={}: {}",
                    className, methodName, duration, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        return Arrays.stream(args)
                .map(this::sanitizeSingleArg)
                .collect(Collectors.joining(", "));
    }

    private String sanitizeSingleArg(Object arg) {
        if (arg == null) return "null";
        String className = arg.getClass().getSimpleName();
        String value = arg.toString();
        if (className.toLowerCase().contains("login") || className.toLowerCase().contains("password")
                || className.toLowerCase().contains("token") || className.toLowerCase().contains("credential")) {
            return className + "[REDACTED]";
        }
        if (value.length() > 100) {
            return className + "[...]";
        }
        return value;
    }

    private String summarize(Object result) {
        if (result == null) return "void";
        String className = result.getClass().getSimpleName();
        String value = result.toString();
        if (value.length() > 150) {
            return className + "[...]";
        }
        return value;
    }
}
