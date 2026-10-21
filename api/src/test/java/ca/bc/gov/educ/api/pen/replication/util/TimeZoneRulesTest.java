package ca.bc.gov.educ.api.pen.replication.util;

import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRulesProvider;

import static org.junit.Assert.assertEquals;

public class TimeZoneRulesTest {

  @Test
  public void shouldKeepVancouverAtUtcMinusSevenOnNovemberFirst2026() {
    final ZoneId vancouver = ZoneId.of("America/Vancouver");
    final ZonedDateTime result = Instant.parse("2026-11-01T10:00:00Z")
        .atZone(vancouver);

    System.out.println("System default zone: " + ZoneId.systemDefault());
    System.out.println("Vancouver result: " + result);
    System.out.println("Vancouver tzdb version: "
        + ZoneRulesProvider.getVersions("America/Vancouver").lastKey());

    assertEquals(ZoneOffset.ofHours(-7), result.getOffset());
    assertEquals(3, result.getHour());
  }
}
