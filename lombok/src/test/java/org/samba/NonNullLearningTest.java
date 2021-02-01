package org.samba;

import lombok.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class NonNullLearningTest {

    @Test
    public void invalidResultOnNonNullCheck() {
        var uut = new WordAdder();
        assertThat(uut.add(null, "b")).isEqualTo("nullb");
    }

    @Test
    public void nullPointerExceptionAndMeaningFullErrorMessageWhenNonNullAnnotationIsUsed() {
        var uut = new WordAdder();
        assertThatThrownBy(() -> uut.add2(null, "b"))
                .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("left is marked non-null but is null");
    }

    static class WordAdder {
        public String add(String left, String right) {
            return left + right;
        }

        public String add2(@NonNull String left, @NonNull String right) {
            return left + right;
        }
    }
}
