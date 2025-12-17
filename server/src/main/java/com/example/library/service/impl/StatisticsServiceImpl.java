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

import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StatisticsServiceImpl(StatisticsMapper statisticsMapper, StringRedisTemplate redisTemplate) {
        this.statisticsMapper = statisticsMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public OverviewVO overview() {
        String cacheKey = "stats:overview";
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cached)) {
                Map<String, Long> cachedMap = objectMapper.readValue(cached, new TypeReference<>() {});
                return buildOverview(cachedMap);
            }
        } catch (Exception ignored) {
        }
        Map<String, Long> data = statisticsMapper.overview();
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(data), Duration.ofMinutes(5));
        } catch (Exception ignored) {
        }
        return buildOverview(data);
    }

    private OverviewVO buildOverview(Map<String, Long> data) {
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
