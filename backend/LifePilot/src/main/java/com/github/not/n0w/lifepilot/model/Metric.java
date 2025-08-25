package com.github.not.n0w.lifepilot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "metrics")
@Data
@NoArgsConstructor
public class Metric {

    public Metric(Long userId, MetricType metricType, Integer value) {
        this.userId = userId;
        this.metricType = metricType;
        this.metricValue = value;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDateTime createdAt = LocalDateTime.now();

    private Integer metricValue;

    @Enumerated(EnumType.STRING)
    private MetricType metricType;

    @Override
    public String toString() {
        return metricType + ": " + metricValue;
    }
}
