package com.example.library.service;

import com.example.library.model.vo.CategoryShareVO;
import com.example.library.model.vo.FrequencyBucketVO;
import com.example.library.model.vo.OverviewVO;
import com.example.library.model.vo.StatusDistributionVO;
import com.example.library.model.vo.TimeCountVO;
import com.example.library.model.vo.TopBookVO;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

    OverviewVO overview();

    List<TimeCountVO> borrowTrend(String granularity, LocalDate start, LocalDate end);

    List<CategoryShareVO> categoryShare(LocalDate start, LocalDate end);

    List<TopBookVO> topBooks(LocalDate start, LocalDate end, Integer limit);

    List<FrequencyBucketVO> readerFrequency(LocalDate start, LocalDate end);

    List<StatusDistributionVO> statusDistribution(LocalDate start, LocalDate end);
}
