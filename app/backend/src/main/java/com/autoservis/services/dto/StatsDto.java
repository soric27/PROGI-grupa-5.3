package com.autoservis.services.dto;

import java.time.LocalDate;
import java.util.List;

public class StatsDto {
  public LocalDate from;
  public LocalDate to;

  public long prijaveCount;
  public long completedRepairsCount;
  public double averageRepairDays;

  public double replacementOccupancyPercent;
  public long availableSlotsCount;
  public List<String> availableSlots; // ISO strings

  public StatsDto() {}
}