package com.example.library.service.impl;

import com.example.library.mapper.StatisticsMapper;
import com.example.library.model.vo.CategoryShareVO;
import com.example.library.model.vo.FrequencyBucketVO;
import com.example.library.model.vo.OverviewVO;
import com.example.library.model.vo.StatusDistributionVO;
import com.example.library.model.vo.TimeCountVO;
import com.example.library.model.vo.TopBookVO;
import com.example.library.service.StatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;

    public StatisticsServiceImpl(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    @Override
    public OverviewVO overview() {
        Map<String, Long> data = statisticsMapper.overview();
        return OverviewVO.builder()
                .totalBorrow(data.getOrDefault("totalBorrow", 0L))
                .totalReaders(data.getOrDefault("totalReaders", 0L))
                .totalBooks(data.getOrDefault("totalBooks", 0L))
                .unreturned(data.getOrDefault("unreturned", 0L))
                .recent7Days(data.getOrDefault("recent7Days", 0L))
                .build();
    }

    @Override
    public List<TimeCountVO> borrowTrend(String granularity, LocalDate start, LocalDate end) {
        String g = "month".equalsIgnoreCase(granularity) ? "month" : "day";
        return statisticsMapper.borrowTrend(g, start, end).stream()
                .map(map -> new TimeCountVO(String.valueOf(map.get("period")), ((Number) map.get("cnt")).longValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryShareVO> categoryShare(LocalDate start, LocalDate end) {
        return statisticsMapper.categoryShare(start, end).stream()
                .map(map -> new CategoryShareVO(String.valueOf(map.get("category")), ((Number) map.get("cnt")).longValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TopBookVO> topBooks(LocalDate start, LocalDate end, Integer limit) {
        Integer resolvedLimit = (limit == null || limit <= 0) ? 10 : limit;
        return statisticsMapper.topBooks(start, end, resolvedLimit).stream()
                .map(map -> TopBookVO.builder()
                        .bookId(((Number) map.get("bookId")).longValue())
                        .title(String.valueOf(map.get("title")))
                        .count(((Number) map.get("cnt")).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<FrequencyBucketVO> readerFrequency(LocalDate start, LocalDate end) {
        return statisticsMapper.readerFrequency(start, end).stream()
                .map(map -> new FrequencyBucketVO(String.valueOf(map.get("bucket")), ((Number) map.get("cnt")).longValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<StatusDistributionVO> statusDistribution(LocalDate start, LocalDate end) {
        return statisticsMapper.statusDistribution(start, end).stream()
                .map(map -> new StatusDistributionVO(String.valueOf(map.get("status")), ((Number) map.get("cnt")).longValue()))
                .collect(Collectors.toList());
    }
}
