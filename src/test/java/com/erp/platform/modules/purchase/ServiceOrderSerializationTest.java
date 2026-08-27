package com.erp.platform.modules.purchase;

import com.erp.platform.modules.purchase.entity.ServiceOrder;
import com.erp.platform.modules.purchase.entity.ServiceOrderItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * A service order with items has to survive being turned into JSON.
 *
 * <p>The order holds its items and each item holds the order back, so serialising one walks the
 * cycle unless a side of it is cut. The list screen swallows a failed request and shows an empty
 * grid, so a break here looks like "saved but not listed" rather than an error.
 */
class ServiceOrderSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("An order with a line serialises without walking back into itself")
    void orderWithItemsSerialises() {
        ServiceOrder order = new ServiceOrder();
        order.setOrderNumber("SVCO-TEST-1");
        order.setTotalAmount(new BigDecimal("100.00"));

        ServiceOrderItem item = new ServiceOrderItem();
        item.setDescription("A line");
        item.setQuantity(BigDecimal.ONE);
        item.setUnitRate(new BigDecimal("100.00"));
        item.setServiceOrder(order);   // the back-reference the service sets on save
        order.getItems().add(item);

        assertThatCode(() -> {
            String json = mapper.writeValueAsString(order);
            assertThat(json).contains("SVCO-TEST-1");
            assertThat(json).as("the line is still reported").contains("A line");
        }).doesNotThrowAnyException();
    }
}
