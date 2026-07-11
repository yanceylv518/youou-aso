package com.youou.aso.modules.account.service;

import com.youou.aso.common.api.PageResult;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.CustomerAccount;
import com.youou.aso.modules.account.dto.CustomerAccountResult;
import com.youou.aso.modules.account.repository.CustomerAccountRepository;
import com.youou.aso.modules.wallet.repository.WalletQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminCustomerService {
    private final CustomerAccountRepository customerAccountRepository;
    private final WalletQueryRepository walletQueryRepository;

    public AdminCustomerService(
            CustomerAccountRepository customerAccountRepository,
            WalletQueryRepository walletQueryRepository
    ) {
        this.customerAccountRepository = customerAccountRepository;
        this.walletQueryRepository = walletQueryRepository;
    }

    public List<CustomerAccountResult> listCustomers(String keyword) {
        return customerAccountRepository.findAll(keyword).stream()
                .map(this::toResult)
                .toList();
    }

    public PageResult<CustomerAccountResult> pageCustomers(String keyword, Integer page, Integer pageSize) {
        Page normalized = normalizePage(page, pageSize);
        List<CustomerAccountResult> customers = customerAccountRepository.findAll(keyword, normalized.pageSize(), normalized.offset()).stream()
                .map(this::toResult)
                .toList();
        long total = customerAccountRepository.countAll(keyword);
        return new PageResult<>(customers, normalized.page(), normalized.pageSize(), total);
    }

    public CustomerAccountResult updateStatus(Long customerId, AccountStatus status) {
        if (status == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        customerAccountRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        customerAccountRepository.updateStatus(customerId, status);
        CustomerAccount updated = customerAccountRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        return toResult(updated);
    }

    private CustomerAccountResult toResult(CustomerAccount customer) {
        return CustomerAccountResult.from(
                customer,
                walletQueryRepository.findAccountByCustomerId(customer.getId()).orElse(null)
        );
    }

    private Page normalizePage(Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        return new Page(safePage, safePageSize);
    }

    private record Page(int page, int pageSize) {
        int offset() {
            return (page - 1) * pageSize;
        }
    }
}
