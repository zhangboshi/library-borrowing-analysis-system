package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.model.vo.CategoryShareVO;
import com.example.library.model.vo.FrequencyBucketVO;
import com.example.library.model.vo.OverviewVO;
import com.example.library.model.vo.StatusDistributionVO;
import com.example.library.model.vo.TimeCountVO;
import com.example.library.model.vo.TopBookVO;
import com.example.library.service.StatisticsService;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@Validated
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    public ApiResponse<OverviewVO> overview() {
        return ApiResponse.success(statisticsService.overview());
    }

    @GetMapping("/borrow-trend")
    public ApiResponse<List<TimeCountVO>> borrowTrend(
            @RequestParam(defaultValue = "day") @Pattern(regexp = "day|month", message = "granularity must be day or month") String granularity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
        return ApiResponse.success(statisticsService.borrowTrend(granularity, start, end));
    }

    @GetMapping("/category-share")
    public ApiResponse<List<CategoryShareVO>> categoryShare(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
        return ApiResponse.success(statisticsService.categoryShare(start, end));
    }

    @GetMapping("/top-books")
    public ApiResponse<List<TopBookVO>> topBooks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "10") Integer limit) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
        return ApiResponse.success(statisticsService.topBooks(start, end, limit));
    }

    @GetMapping("/reader-frequency")
    public ApiResponse<List<FrequencyBucketVO>> readerFrequency(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
        return ApiResponse.success(statisticsService.readerFrequency(start, end));
    }

    @GetMapping("/status-distribution")
    public ApiResponse<List<StatusDistributionVO>> statusDistribution(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
        return ApiResponse.success(statisticsService.statusDistribution(start, end));
    }
}
