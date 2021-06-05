package org.samba;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DateTimeLearningTest {

  @Test
  public void nextWorkDayForMondayIsTuesday() {
    val someMonday = ZonedDateTime.of(2021, 6, 14, 12, 0, 0, 0, ZoneId.systemDefault());
    // following Tuesday
    val expected = ZonedDateTime.of(2021, 6, 15, 12, 0, 0, 0, ZoneId.systemDefault());
    assertThat(someMonday.with(new NextWorkingDay())).isEqualTo(expected);
  }

  @Test
  void nextWorkDayForFridayIsMonday() {
    val someFriday = ZonedDateTime.of(2021, 6, 4, 12, 0, 0, 0, ZoneId.systemDefault());
    // following Monday
    val expected = ZonedDateTime.of(2021, 6, 7, 12, 0, 0, 0, ZoneId.systemDefault());
    assertThat(someFriday.with(new NextWorkingDay())).isEqualTo(expected);
  }

  private class NextWorkingDay implements TemporalAdjuster {
    @Override
    public Temporal adjustInto(Temporal currentDay) {
      var weekendDays = List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
      Temporal nextWorkDay = currentDay;
      do {
        nextWorkDay = nextWorkDay.plus(1L, ChronoUnit.DAYS);

      } while (weekendDays.contains(toDayOfWeek(nextWorkDay)));
      return nextWorkDay;
    }

    private DayOfWeek toDayOfWeek(Temporal nextWorkDay) {
      return DayOfWeek.of(nextWorkDay.get(ChronoField.DAY_OF_WEEK));
    }
  }
}
