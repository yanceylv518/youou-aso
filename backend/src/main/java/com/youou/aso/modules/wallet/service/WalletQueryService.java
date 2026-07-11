package com.youou.aso.modules.wallet.service;

import com.youou.aso.common.api.PageResult;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.wallet.dto.WalletOverviewResult;
import com.youou.aso.modules.wallet.dto.WalletTransactionResult;
import com.youou.aso.modules.wallet.domain.WalletDirection;
import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import com.youou.aso.modules.wallet.repository.WalletQueryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class WalletQueryService {
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final WalletQueryRepository walletQueryRepository;

    public WalletQueryService(WalletQueryRepository walletQueryRepository) {
        this.walletQueryRepository = walletQueryRepository;
    }

    public WalletOverviewResult getCustomerWallet(Long customerId) {
        if (customerId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return walletQueryRepository.findAccountByCustomerId(customerId)
                .map(WalletOverviewResult::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));
    }

    public List<WalletTransactionResult> listCustomerTransactions(Long customerId, Integer limit) {
        if (customerId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return walletQueryRepository.findTransactionsByCustomerId(customerId, normalizeLimit(limit))
                .stream()
                .map(WalletTransactionResult::from)
                .toList();
    }

    public List<WalletTransactionResult> listAdminTransactions(Long customerId, WalletTransactionType transactionType, Integer limit) {
        return walletQueryRepository.findTransactions(customerId, transactionType, normalizeLimit(limit))
                .stream()
                .map(WalletTransactionResult::from)
                .toList();
    }

    public PageResult<WalletTransactionResult> pageCustomerTransactions(
            Long customerId,
            WalletTransactionType transactionType,
            WalletDirection direction,
            OrderType orderType,
            LocalDate createdDateFrom,
            LocalDate createdDateTo,
            Integer page,
            Integer pageSize
    ) {
        if (customerId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return pageTransactions(customerId, transactionType, direction, orderType, createdDateFrom, createdDateTo, page, pageSize);
    }

    public PageResult<WalletTransactionResult> pageAdminTransactions(
            Long customerId,
            WalletTransactionType transactionType,
            WalletDirection direction,
            OrderType orderType,
            LocalDate createdDateFrom,
            LocalDate createdDateTo,
            Integer page,
            Integer pageSize
    ) {
        return pageTransactions(customerId, transactionType, direction, orderType, createdDateFrom, createdDateTo, page, pageSize);
    }

    private PageResult<WalletTransactionResult> pageTransactions(
            Long customerId,
            WalletTransactionType transactionType,
            WalletDirection direction,
            OrderType orderType,
            LocalDate createdDateFrom,
            LocalDate createdDateTo,
            Integer page,
            Integer pageSize
    ) {
        Page pageInfo = normalizePage(page, pageSize);
        long total = walletQueryRepository.countTransactions(customerId, transactionType, direction, orderType, createdDateFrom, createdDateTo);
        List<WalletTransactionResult> items = total == 0
                ? List.of()
                : walletQueryRepository.findTransactions(
                                customerId,
                                transactionType,
                                direction,
                                orderType,
                                createdDateFrom,
                                createdDateTo,
                                pageInfo.pageSize(),
                                pageInfo.offset()
                        )
                        .stream()
                        .map(WalletTransactionResult::from)
                        .toList();
        return new PageResult<>(items, pageInfo.page(), pageInfo.pageSize(), total);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private Page normalizePage(Integer page, Integer pageSize) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        return new Page(normalizedPage, normalizedPageSize);
    }

    private record Page(int page, int pageSize) {
        int offset() {
            return (page - 1) * pageSize;
        }
    }
}
