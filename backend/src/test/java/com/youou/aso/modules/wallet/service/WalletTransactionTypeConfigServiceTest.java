package com.youou.aso.modules.wallet.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import com.youou.aso.modules.wallet.domain.WalletTransactionTypeConfig;
import com.youou.aso.modules.wallet.dto.UpdateWalletTransactionTypeConfigCommand;
import com.youou.aso.modules.wallet.dto.WalletTransactionTypeConfigResult;
import com.youou.aso.modules.wallet.repository.WalletTransactionTypeConfigRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletTransactionTypeConfigServiceTest {
    private final FakeWalletTransactionTypeConfigRepository repository = new FakeWalletTransactionTypeConfigRepository();
    private final WalletTransactionTypeConfigService service = new WalletTransactionTypeConfigService(repository);

    @Test
    void listConfigsReturnsAllKnownTypesWithDefaults() {
        List<WalletTransactionTypeConfigResult> results = service.listConfigs();

        assertThat(results).extracting(WalletTransactionTypeConfigResult::transactionType)
                .containsExactly(WalletTransactionType.values());
        assertThat(results.get(0).displayNameZh()).isEqualTo("订单扣款");
        assertThat(results.get(0).displayNameEn()).isEqualTo("Order deduction");
    }

    @Test
    void updateConfigsOnlyChangesDisplayNamesForExistingTypes() {
        List<WalletTransactionTypeConfigResult> results = service.updateConfigs(List.of(
                new UpdateWalletTransactionTypeConfigCommand(WalletTransactionType.ORDER_DEDUCT, "消费扣款", "Consumption"),
                new UpdateWalletTransactionTypeConfigCommand(WalletTransactionType.ORDER_REFUND, "订单返款", "Refund"),
                new UpdateWalletTransactionTypeConfigCommand(WalletTransactionType.ADMIN_RECHARGE, "客户充值", "Recharge"),
                new UpdateWalletTransactionTypeConfigCommand(WalletTransactionType.ADMIN_ADJUSTMENT, "人工调整", "Adjustment"),
                new UpdateWalletTransactionTypeConfigCommand(WalletTransactionType.DELIVERY, "配送", "Delivery")
        ));

        assertThat(results).hasSize(WalletTransactionType.values().length);
        assertThat(results.get(0).transactionType()).isEqualTo(WalletTransactionType.ORDER_DEDUCT);
        assertThat(results.get(0).displayNameZh()).isEqualTo("消费扣款");
        assertThat(results.get(0).displayNameEn()).isEqualTo("Consumption");
    }

    @Test
    void updateConfigsRejectsBlankDisplayName() {
        assertThatThrownBy(() -> service.updateConfigs(List.of(
                new UpdateWalletTransactionTypeConfigCommand(WalletTransactionType.ORDER_DEDUCT, " ", "Consumption")
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFIG_VALUE_INVALID);
    }

    private static final class FakeWalletTransactionTypeConfigRepository implements WalletTransactionTypeConfigRepository {
        private final Map<WalletTransactionType, WalletTransactionTypeConfig> configs = new EnumMap<>(WalletTransactionType.class);

        @Override
        public List<WalletTransactionTypeConfig> findAll() {
            return new ArrayList<>(configs.values());
        }

        @Override
        public void saveAll(List<WalletTransactionTypeConfig> nextConfigs) {
            for (WalletTransactionTypeConfig config : nextConfigs) {
                configs.put(config.getTransactionType(), config);
            }
        }
    }
}
