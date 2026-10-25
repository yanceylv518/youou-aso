package com.youou.aso.modules.appmanagement.service;

import com.youou.aso.common.error.*;
import com.youou.aso.modules.appmanagement.domain.*;
import com.youou.aso.modules.appmanagement.repository.CustomerAppRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;

@Service
public class AppAvailabilityService {
    private final CustomerAppRepository repository;
    private final AppStoreVerifier verifier;
    private final TransactionTemplate independent;

    public AppAvailabilityService(CustomerAppRepository repository, AppStoreVerifier verifier, PlatformTransactionManager transactions) {
        this.repository = repository;
        this.verifier = verifier;
        independent = new TransactionTemplate(transactions);
        independent.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void requireAvailable(CustomerApp app) {
        if (app.getStatus() != CustomerAppStatus.ACTIVE) throw new BusinessException(ErrorCode.APP_NOT_VERIFIED);
        if (!isListed(app)) {
            // Commit the detection separately: rejecting the order must not undo the disabled status.
            independent.executeWithoutResult(tx -> repository.updateStatus(app.getId(), CustomerAppStatus.DISABLED));
            throw new BusinessException(ErrorCode.APP_DELISTED);
        }
    }

    public CustomerAppStatus recheck(Long customerId, Long id) {
        CustomerApp app = repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (customerId != null && !customerId.equals(app.getCustomerId())) throw new BusinessException(ErrorCode.FORBIDDEN);
        boolean listed = isListed(app);
        CustomerAppStatus status = listed ? CustomerAppStatus.ACTIVE : CustomerAppStatus.DISABLED;
        independent.executeWithoutResult(tx -> {
            if (listed && repository.findActiveByCustomerStoreAndIdentifier(app.getCustomerId(), app.getStoreType(), app.getAppIdentifier())
                    .filter(other -> !other.getId().equals(id)).isPresent()) throw new BusinessException(ErrorCode.APP_ALREADY_EXISTS);
            repository.updateStatus(id, status);
        });
        return status;
    }

    private boolean isListed(CustomerApp app) {
        List<String> regions = repository.findRegionCodesByAppIds(List.of(app.getId())).getOrDefault(app.getId(), List.of(app.getRegionCode()));
        if (regions.isEmpty()) regions = List.of(app.getRegionCode());
        // A regional withdrawal alone does not prove the entire application is delisted.
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
        BusinessException unavailable = null;
        for (String region : regions) {
            if (System.nanoTime() > deadline) throw new BusinessException(ErrorCode.APP_VERIFICATION_FAILED);
            try {
                if (verifier.verify(app.getStoreType(), region, app.getAppIdentifier()).isPresent()) return true;
            } catch (RuntimeException ex) {
                unavailable = new BusinessException(ErrorCode.APP_VERIFICATION_FAILED);
            }
        }
        if (unavailable != null) throw unavailable;
        return false;
    }
}
