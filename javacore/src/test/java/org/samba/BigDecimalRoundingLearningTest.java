package org.samba;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;

public class BigDecimalRoundingLearningTest {

    @Test
    public void withoutAnyRoundingConfig() {
        // gives: Non-terminating decimal expansion; no exact representable decimal result
        assertThatThrownBy(
                () -> new BigDecimal(1).divide( new BigDecimal(3) ))
                .isInstanceOf(ArithmeticException.class);
    }


    @Test
    public void withRoundingConfig() {
        var a = BigDecimal.valueOf(1);
        var b = BigDecimal.valueOf(3);
        var nachKommaStellen = 5;

        var result = a.divide(b, nachKommaStellen, RoundingMode.HALF_DOWN);
        assertThat(result).isEqualTo("0.33333");
    }

    @Test
    public void withCentralRoundingConfig() {
        var a = BigDecimal.valueOf(1);
        var b = BigDecimal.valueOf(3);
        var nachKommaStellen = 5;
        var roundingConfig = new MathContext(nachKommaStellen, RoundingMode.HALF_DOWN);

        var result = a.divide(b, roundingConfig);
        assertThat(result).isEqualTo("0.33333");
    }

}
