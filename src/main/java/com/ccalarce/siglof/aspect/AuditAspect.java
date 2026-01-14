package com.ccalarce.siglof.aspect;

import com.ccalarce.siglof.annotation.Auditable;
import com.ccalarce.siglof.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void auditMethod(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String action = auditable.action();

            // Serialize arguments (request)
            Object[] args = joinPoint.getArgs();
            String details = "Args: " + safeSerialize(args) + " | Result: " + safeSerialize(result);

            if (details.length() > 500)
                details = details.substring(0, 500) + "..."; // Truncate to avoid huge logs

            auditService.log(action, className, details);

        } catch (Exception e) {
            System.err.println("Audit failed: " + e.getMessage());
        }
    }

    private String safeSerialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "Unserializable";
        }
    }
}
