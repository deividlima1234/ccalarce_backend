package com.ccalarce.siglof.service;

import com.ccalarce.siglof.model.entity.AuditLog;
import com.ccalarce.siglof.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // Ensure audit is saved even if main logic fails? Or
                                                           // strictly with it? Usually separate txn is safer for
                                                           // logging failures, but for auditing successful actions,
                                                           // same txn is ok. "REQUIRES_NEW" ensures we log attempts
                                                           // even if they fail? No, if we audit *after* return. Let's
                                                           // stick to standard propagation but maybe async later.
    public void log(String action, String resource, String details) {
        String username = "ANONYMOUS";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
        }

        AuditLog log = AuditLog.builder()
                .username(username)
                .action(action)
                .resource(resource)
                .details(details)
                .build();

        auditRepository.save(log);
    }
}
