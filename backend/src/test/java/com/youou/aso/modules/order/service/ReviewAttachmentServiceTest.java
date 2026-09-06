package com.youou.aso.modules.order.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.domain.OrderCommentDetail;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.dto.CreateOrderCommand;
import com.youou.aso.modules.order.dto.OrderResult;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mock.web.MockMultipartFile;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ReviewAttachmentServiceTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final ReviewAttachmentService service = new ReviewAttachmentService(jdbc);

    @Test
    void rejectsEmptyAndUnsupportedFiles() {
        assertThatThrownBy(() -> service.upload(10L, new MockMultipartFile("file", "reviews.csv", "text/csv", new byte[0])))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.upload(10L, new MockMultipartFile("file", "reviews.html", "text/html", "hello".getBytes())))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(jdbc);
    }

    @Test
    void preservesOriginalFileBytesWithoutParsingRows() {
        byte[] bytes = new byte[]{0, 1, 2, 3, 4};
        var result = service.upload(10L, new MockMultipartFile("file", "reviews.xlsx", "application/octet-stream", bytes));
        assertThat(result.fileName()).isEqualTo("reviews.xlsx");
        assertThat(result.fileSize()).isEqualTo(bytes.length);
        verify(jdbc).update(anyString(), eq(result.id()), eq(10L), eq("reviews.xlsx"), eq(5L), eq(bytes));
    }

    @Test
    void refusesBindingAnotherCustomersAttachment() {
        AsoOrder order = new AsoOrder();
        order.setId(1L);
        order.setCustomerId(10L);
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("other-file"), eq(10L))).thenReturn(0);
        var command = new CreateOrderCommand(1L, "US", OrderType.REVIEW,
                LocalDate.now(), LocalDate.now(), null, List.of(), null,
                List.of(new CreateOrderCommand.RegionOrderItem("US", null, null, null, 5, 4, List.of("other-file"))),
                null, null, null, null, null, null);
        assertThatThrownBy(() -> service.bind(order, command)).isInstanceOf(BusinessException.class);
        verify(jdbc, never()).update(startsWith("INSERT INTO aso_order_review_attachment"), any(), any(), any());
    }

    @Test
    void refusesDownloadingAnotherCustomersFileOrLegacyComments() {
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<ReviewAttachmentService.Download>>any(), eq("other-file"), eq(10L)))
                .thenReturn(List.of());
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(2L), eq(10L))).thenReturn(0);
        assertThatThrownBy(() -> service.download("other-file", 10L)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.download("legacy:2:US", 10L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void orderResponsesNeverContainCommentBodies() {
        AsoOrder order = new AsoOrder();
        OrderCommentDetail detail = new OrderCommentDetail();
        detail.setCommentContent("A large historical comment body");
        order.setCommentDetails(List.of(detail));
        assertThat(OrderResult.from(order).commentDetails()).isEmpty();
    }
}
