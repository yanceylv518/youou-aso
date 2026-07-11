package com.youou.aso.modules.wallet.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import com.youou.aso.modules.wallet.domain.WalletTransactionTypeConfig;
import com.youou.aso.modules.wallet.dto.UpdateWalletTransactionTypeConfigCommand;
import com.youou.aso.modules.wallet.dto.WalletTransactionTypeConfigResult;
import com.youou.aso.modules.wallet.repository.WalletTransactionTypeConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class WalletTransactionTypeConfigService {
    private static final int MAX_ZH_LENGTH = 80;
    private static final int MAX_EN_LENGTH = 120;

    private final WalletTransactionTypeConfigRepository repository;

    public WalletTransactionTypeConfigService(WalletTransactionTypeConfigRepository repository) {
        this.repository = repository;
    }

    public List<WalletTransactionTypeConfigResult> listConfigs() {
        Map<WalletTransactionType, WalletTransactionTypeConfig> existing = new EnumMap<>(WalletTransactionType.class);
        repository.findAll().forEach(config -> existing.put(config.getTransactionType(), config));

        return Arrays.stream(WalletTransactionType.values())
                .map(type -> existing.getOrDefault(type, WalletTransactionTypeConfig.defaultFor(type)))
                .map(WalletTransactionTypeConfigResult::from)
                .toList();
    }

    public List<WalletTransactionTypeConfigResult> updateConfigs(List<UpdateWalletTransactionTypeConfigCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Map<WalletTransactionType, UpdateWalletTransactionTypeConfigCommand> requested = new EnumMap<>(WalletTransactionType.class);
        for (UpdateWalletTransactionTypeConfigCommand command : commands) {
            validate(command);
            requested.put(command.transactionType(), command);
        }

        List<WalletTransactionTypeConfig> configs = Arrays.stream(WalletTransactionType.values())
                .map(type -> toConfig(type, requested.get(type)))
                .toList();
        repository.saveAll(configs);
        return listConfigs();
    }

    private WalletTransactionTypeConfig toConfig(WalletTransactionType type, UpdateWalletTransactionTypeConfigCommand command) {
        if (command == null) {
            return WalletTransactionTypeConfig.defaultFor(type);
        }
        WalletTransactionTypeConfig config = new WalletTransactionTypeConfig();
        config.setTransactionType(type);
        config.setDisplayNameZh(command.displayNameZh().trim());
        config.setDisplayNameEn(command.displayNameEn().trim());
        return config;
    }

    private void validate(UpdateWalletTransactionTypeConfigCommand command) {
        if (command == null || command.transactionType() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (isBlank(command.displayNameZh()) || isBlank(command.displayNameEn())) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }
        if (command.displayNameZh().trim().length() > MAX_ZH_LENGTH || command.displayNameEn().trim().length() > MAX_EN_LENGTH) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
