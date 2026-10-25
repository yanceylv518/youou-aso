package com.youou.aso.modules.appmanagement.service;

import com.youou.aso.common.error.*;
import com.youou.aso.modules.appmanagement.domain.*;
import com.youou.aso.modules.appmanagement.repository.CustomerAppRepository;
import org.junit.jupiter.api.*;
import org.springframework.transaction.*;
import org.springframework.transaction.support.SimpleTransactionStatus;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppAvailabilityServiceTest {
    CustomerAppRepository repository = mock(CustomerAppRepository.class);
    AppStoreVerifier verifier = mock(AppStoreVerifier.class);
    PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    AppAvailabilityService service = new AppAvailabilityService(repository, verifier, transactions);
    CustomerApp app = new CustomerApp();
    @BeforeEach void setup() {
        app.setId(1L); app.setCustomerId(2L); app.setStoreType(StoreType.APP_STORE);
        app.setAppIdentifier("123"); app.setRegionCode("US"); app.setStatus(CustomerAppStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(app));
        when(transactions.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }
    @Test void delistingCommitsIndependentlyBeforeRejectingOrder() {
        var ex = assertThrows(BusinessException.class, () -> service.requireAvailable(app));
        assertEquals(ErrorCode.APP_DELISTED, ex.getErrorCode());
        verify(repository).updateStatus(1L, CustomerAppStatus.DISABLED);
        verify(transactions).getTransaction(argThat(def -> def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        verify(transactions).commit(any());
        verify(transactions, never()).rollback(any());
    }
    @Test void unknownStoreFailureNeverChangesStatus() {
        when(verifier.verify(any(), any(), any())).thenThrow(new IllegalStateException("timeout"));
        assertEquals(ErrorCode.APP_VERIFICATION_FAILED, assertThrows(BusinessException.class, () -> service.requireAvailable(app)).getErrorCode());
        verify(repository, never()).updateStatus(any(), any());
        verifyNoInteractions(transactions);
    }
    @Test void oneAvailableRegionPreventsGlobalDisable() {
        when(repository.findRegionCodesByAppIds(any())).thenReturn(Map.of(1L, List.of("US", "JP")));
        when(verifier.verify(StoreType.APP_STORE, "JP", "123")).thenReturn(Optional.of(listed()));
        service.requireAvailable(app);
        verify(repository, never()).updateStatus(any(), any());
    }
    @Test void relistedApplicationCanBeEnabledByItsOwner() {
        app.setStatus(CustomerAppStatus.DISABLED);
        when(verifier.verify(any(), any(), any())).thenReturn(Optional.of(listed()));
        assertEquals(CustomerAppStatus.ACTIVE, service.recheck(2L, 1L));
        verify(repository).updateStatus(1L, CustomerAppStatus.ACTIVE);
    }
    @Test void anotherCustomerCannotCheckOrEnableTheApp() {
        assertEquals(ErrorCode.FORBIDDEN, assertThrows(BusinessException.class, () -> service.recheck(99L, 1L)).getErrorCode());
        verifyNoInteractions(verifier, transactions);
    }
    @Test void manualCheckCanConfirmStillDelisted() {
        assertEquals(CustomerAppStatus.DISABLED, service.recheck(null, 1L));
        verify(repository).updateStatus(1L, CustomerAppStatus.DISABLED);
    }
    @Test void duplicateActiveApplicationPreventsReactivation() {
        var other = new CustomerApp(); other.setId(3L);
        when(repository.findActiveByCustomerStoreAndIdentifier(any(), any(), any())).thenReturn(Optional.of(other));
        when(verifier.verify(any(), any(), any())).thenReturn(Optional.of(listed()));
        assertEquals(ErrorCode.APP_ALREADY_EXISTS, assertThrows(BusinessException.class, () -> service.recheck(2L, 1L)).getErrorCode());
        verify(repository, never()).updateStatus(any(), any());
    }
    private VerifiedStoreApp listed() { return new VerifiedStoreApp(StoreType.APP_STORE, "US", "123", "App", null, null, "123", null); }
}
