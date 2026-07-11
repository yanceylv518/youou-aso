package com.youou.aso.modules.account.dto;

import java.util.List;

public record AdminAccessResult(
        List<String> roleKeys,
        List<String> menuCodes,
        List<String> permissions
) {
    public static AdminAccessResult empty() {
        return new AdminAccessResult(List.of(), List.of(), List.of());
    }
}
