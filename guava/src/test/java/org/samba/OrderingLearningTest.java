package org.samba;

import com.google.common.base.Preconditions;
import com.google.common.collect.Comparators;
import com.google.common.collect.Ordering;
import lombok.NonNull;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class OrderingLearningTest {

    @Test
    public void precondition() {
        assertThatThrownBy(() -> sayName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("'name' is invalid");
    }

    private void sayName(String name) {
        Preconditions.checkArgument(! Strings.isNullOrEmpty(name), "'name' is invalid");
        System.out.println("You are " + name);
    }

    @Test
    public void findYoungestTimestampViaGuavaOrdering() {
        var yesterday = ZonedDateTime.now().minusDays(1);
        var tomorrow = ZonedDateTime.now().plusDays(1);

        var youngestTimestamp = Ordering.natural().max(tomorrow, yesterday);
        assertThat(youngestTimestamp).isEqualTo(tomorrow);
    }

    @Test
    public void findYoungestTimestampViaNewerGuavaComperatorsClass() {
        var yesterday = ZonedDateTime.now().minusDays(1);
        var tomorrow = ZonedDateTime.now().plusDays(1);

        var youngestTimestamp = Comparators.max(tomorrow, yesterday);
        assertThat(youngestTimestamp).isEqualTo(tomorrow);
    }


    @Test
    public void findYoungestTimestampViaJavaCore() {
        var yesterday = ZonedDateTime.now().minusDays(1);
        var tomorrow = ZonedDateTime.now().plusDays(1);

        var youngestTimestamp = Stream.of(tomorrow, yesterday)
                .max(Comparator.naturalOrder())
                .get();
        assertThat(youngestTimestamp).isEqualTo(tomorrow);
    }
}
