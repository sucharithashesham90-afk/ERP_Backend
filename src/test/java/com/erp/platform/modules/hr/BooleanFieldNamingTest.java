package com.erp.platform.modules.hr;

import com.erp.platform.modules.hr.dto.PayGradeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The JSON names of the is-prefixed booleans the screens read.
 *
 * <p>Lombok turns a field named {@code isLossOfPayApplicable} into a getter
 * {@code isLossOfPayApplicable()}, from which Jackson derives the property name
 * {@code lossOfPayApplicable} — dropping the "is". A screen reading
 * {@code row.isLossOfPayApplicable} then gets undefined and shows the switch as off, which looks
 * exactly like the value never having been saved. The value was saved; the response just called it
 * something else.
 */
class BooleanFieldNamingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Pay grade sends the boolean names the screen reads")
    void payGradeBooleansKeepTheirIsPrefix() throws Exception {
        PayGradeDto dto = new PayGradeDto();
        dto.setLossOfPayApplicable(true);
        dto.setOverTimeApplicable(true);

        String json = mapper.writeValueAsString(dto);

        assertThat(json)
                .as("the screen reads row.isLossOfPayApplicable")
                .contains("\"isLossOfPayApplicable\":true");
        assertThat(json)
                .as("the screen reads row.isOverTimeApplicable")
                .contains("\"isOverTimeApplicable\":true");
    }
}
