package com.github.not.n0w.lifepilot.repository;

import com.github.not.n0w.lifepilot.model.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {

    @Query(value = """
    SELECT *
    FROM (
        SELECT m.*,
               ROW_NUMBER() OVER(PARTITION BY m.metric_type ORDER BY m.created_at DESC) AS rn
        FROM metrics m
        WHERE m.user_id = :userId
    ) t
    WHERE t.rn = 1
""", nativeQuery = true)
    List<Metric> findLatestMetricsByUserId(@Param("userId") Long userId);

    @Query(value = """
    SELECT *
    FROM (
        SELECT m.*,
               ROW_NUMBER() OVER(PARTITION BY m.metric_type ORDER BY m.created_at DESC) AS rn
        FROM metrics m
        WHERE m.user_id = :userId
    ) t
    WHERE t.rn = 2
""", nativeQuery = true)
    List<Metric> findPreviousMetricsByUserId(@Param("userId") Long userId);

    List<Metric> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
