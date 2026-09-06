package com.youou.aso.modules.pricing.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.pricing.domain.OrderModuleConfig;
import com.youou.aso.modules.pricing.service.OrderModuleConfigService;
import com.youou.aso.modules.pricing.service.OrderModuleTranslationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
public class OrderModuleConfigController {
    private final OrderModuleConfigService service;
    private final OrderModuleTranslationService translationService;
    public OrderModuleConfigController(OrderModuleConfigService service, OrderModuleTranslationService translationService) {
        this.service = service;
        this.translationService = translationService;
    }

    @GetMapping("/api/customer/order-modules")
    public ApiResponse<List<OrderModuleConfig>> customerList(@RequestParam(required=false) OrderType orderType) {
        return ApiResponse.ok(service.listEnabled(orderType));
    }
    @PreAuthorize("@perm.hasMenu('system.pricing')")
    @GetMapping("/api/admin/order-modules")
    public ApiResponse<List<OrderModuleConfig>> adminList() { return ApiResponse.ok(service.listAll()); }
    @PreAuthorize("@perm.has('pricing:update')")
    @PostMapping("/api/admin/order-modules")
    public ApiResponse<OrderModuleConfig> create(@Valid @RequestBody ModuleRequest r) {
        return ApiResponse.ok(service.save(null,r.moduleName(),r.moduleNameEn(),r.moduleNameRu(),r.moduleNamePt(),r.moduleNameEs(),
                r.moduleDescription(),r.moduleDescriptionEn(),r.moduleDescriptionRu(),r.moduleDescriptionPt(),r.moduleDescriptionEs(),
                r.orderType(),r.unitPrice(),r.chinaUnitPrice(),r.enabled(),r.sortOrder()));
    }
    @PreAuthorize("@perm.has('pricing:update')")
    @PutMapping("/api/admin/order-modules/{id}")
    public ApiResponse<OrderModuleConfig> update(@PathVariable Long id,@Valid @RequestBody ModuleRequest r) {
        return ApiResponse.ok(service.save(id,r.moduleName(),r.moduleNameEn(),r.moduleNameRu(),r.moduleNamePt(),r.moduleNameEs(),
                r.moduleDescription(),r.moduleDescriptionEn(),r.moduleDescriptionRu(),r.moduleDescriptionPt(),r.moduleDescriptionEs(),
                r.orderType(),r.unitPrice(),r.chinaUnitPrice(),r.enabled(),r.sortOrder()));
    }
    @PreAuthorize("@perm.has('pricing:update')")
    @DeleteMapping("/api/admin/order-modules/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { service.delete(id); return ApiResponse.ok(null); }

    @PreAuthorize("@perm.has('pricing:update')")
    @PostMapping("/api/admin/order-modules/translate")
    public ApiResponse<java.util.Map<String, OrderModuleTranslationService.Translation>> translate(
            @Valid @RequestBody TranslationRequest request) {
        return ApiResponse.ok(translationService.translate(
                request.sourceLocale(), request.moduleName(), request.moduleDescription()));
    }

    public record ModuleRequest(@NotBlank @Size(max=100) String moduleName, @NotBlank @Size(max=100) String moduleNameEn, @NotBlank @Size(max=100) String moduleNameRu, @NotBlank @Size(max=100) String moduleNamePt, @NotBlank @Size(max=100) String moduleNameEs,
            @NotBlank @Size(max=500) String moduleDescription, @NotBlank @Size(max=500) String moduleDescriptionEn,
            @NotBlank @Size(max=500) String moduleDescriptionRu, @NotBlank @Size(max=500) String moduleDescriptionPt,
            @NotBlank @Size(max=500) String moduleDescriptionEs, @NotNull OrderType orderType,
            @DecimalMin("0") BigDecimal unitPrice,
            @DecimalMin("0") BigDecimal chinaUnitPrice, boolean enabled, int sortOrder) {}

    public record TranslationRequest(
            @NotBlank @Pattern(regexp="zh-CN|en-US|ru-RU|pt-PT|es-ES") String sourceLocale,
            @Size(max=100) String moduleName,
            @Size(max=500) String moduleDescription) {}
}
