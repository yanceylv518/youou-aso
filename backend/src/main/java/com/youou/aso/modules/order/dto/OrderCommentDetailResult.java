package com.youou.aso.modules.order.dto;

import com.youou.aso.modules.order.domain.OrderCommentDetail;

public record OrderCommentDetailResult(
        Long id,
        String regionCode,
        Integer starLevel,
        String commentTitle,
        String commentContent
) {
    public static OrderCommentDetailResult from(OrderCommentDetail detail) {
        return new OrderCommentDetailResult(
                detail.getId(),
                detail.getRegionCode(),
                detail.getStarLevel(),
                detail.getCommentTitle(),
                detail.getCommentContent()
        );
    }
}
