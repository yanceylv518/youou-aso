package com.youou.aso.modules.order.dto;

import com.youou.aso.modules.order.domain.OrderType;

import java.time.LocalDate;
import java.util.List;

public record CreateOrderCommand(
        Long customerAppId,
        String regionCode,
        OrderType orderType,
        LocalDate startDate,
        LocalDate endDate,
        Integer executionHours,
        List<String> keywords,
        List<KeywordQuantity> keywordItems,
        List<RegionOrderItem> regionItems,
        List<ReviewDetail> reviewDetails,
        Integer dailyDownloadCount,
        Integer rating5Count,
        Integer rating4Count,
        Integer review5Count,
        Integer review4Count
) {
    public CreateOrderCommand(
            Long customerAppId,
            String regionCode,
            OrderType orderType,
            LocalDate startDate,
            LocalDate endDate,
            Integer executionHours,
            String ignoredContactType,
            String ignoredContactValue,
            String ignoredRemark,
            List<String> keywords,
            List<KeywordQuantity> keywordItems,
            List<RegionOrderItem> regionItems,
            List<ReviewDetail> reviewDetails,
            Integer dailyDownloadCount,
            Integer rating5Count,
            Integer rating4Count,
            Integer review5Count,
            Integer review4Count
    ) {
        this(
                customerAppId,
                regionCode,
                orderType,
                startDate,
                endDate,
                executionHours,
                keywords,
                keywordItems,
                regionItems,
                reviewDetails,
                dailyDownloadCount,
                rating5Count,
                rating4Count,
                review5Count,
                review4Count
        );
    }

    public CreateOrderCommand(
            Long customerAppId,
            String regionCode,
            OrderType orderType,
            LocalDate startDate,
            LocalDate endDate,
            Integer executionHours,
            List<String> keywords,
            List<KeywordQuantity> keywordItems,
            List<RegionOrderItem> regionItems,
            Integer dailyDownloadCount,
            Integer rating5Count,
            Integer rating4Count,
            Integer review5Count,
            Integer review4Count
    ) {
        this(
                customerAppId,
                regionCode,
                orderType,
                startDate,
                endDate,
                executionHours,
                keywords,
                keywordItems,
                regionItems,
                null,
                dailyDownloadCount,
                rating5Count,
                rating4Count,
                review5Count,
                review4Count
        );
    }

    public CreateOrderCommand(
            Long customerAppId,
            OrderType orderType,
            LocalDate startDate,
            LocalDate endDate,
            Integer executionHours,
            List<String> keywords,
            Integer dailyDownloadCount,
            Integer rating5Count,
            Integer rating4Count,
            Integer review5Count,
            Integer review4Count
    ) {
        this(
                customerAppId,
                null,
                orderType,
                startDate,
                endDate,
                executionHours,
                keywords,
                null,
                null,
                null,
                dailyDownloadCount,
                rating5Count,
                rating4Count,
                review5Count,
                review4Count
        );
    }

    public CreateOrderCommand(
            Long customerAppId,
            OrderType orderType,
            LocalDate startDate,
            LocalDate endDate,
            Integer executionHours,
            List<String> keywords,
            List<KeywordQuantity> keywordItems,
            Integer dailyDownloadCount,
            Integer rating5Count,
            Integer rating4Count,
            Integer review5Count,
            Integer review4Count
    ) {
        this(
                customerAppId,
                null,
                orderType,
                startDate,
                endDate,
                executionHours,
                keywords,
                keywordItems,
                null,
                null,
                dailyDownloadCount,
                rating5Count,
                rating4Count,
                review5Count,
                review4Count
        );
    }

    public record KeywordQuantity(
            String keyword,
            Integer quantity,
            String regionCode
    ) {
        public KeywordQuantity(String keyword, Integer quantity) {
            this(keyword, quantity, null);
        }
    }

    public record RegionOrderItem(
            String regionCode,
            Integer dailyDownloadCount,
            Integer rating5Count,
            Integer rating4Count,
            Integer review5Count,
            Integer review4Count
    ) {
    }

    public record ReviewDetail(
            String regionCode,
            Integer starLevel,
            String commentTitle,
            String commentContent
    ) {
    }
}
