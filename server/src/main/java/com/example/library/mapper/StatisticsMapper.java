package com.example.library.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    @Select("""
            select
                (select count(*) from borrow_record) as totalBorrow,
                (select count(*) from reader) as totalReaders,
                (select count(*) from book) as totalBooks,
                (select count(*) from borrow_record where status != 'RETURNED') as unreturned,
                (select count(*) from borrow_record where borrow_time >= date_sub(curdate(), interval 6 day)) as recent7Days
            """)
    Map<String, Long> overview();

    @Select("""
            <script>
            select
                <choose>
                    <when test="granularity == 'month'">
                        date_format(borrow_time, '%Y-%m') as period
                    </when>
                    <otherwise>
                        date_format(borrow_time, '%Y-%m-%d') as period
                    </otherwise>
                </choose>,
                count(*) as cnt
            from borrow_record
            where 1=1
            <if test="start != null">and borrow_time &gt;= #{start}</if>
            <if test="end != null">and borrow_time &lt;= #{end}</if>
            group by period
            order by period
            </script>
            """)
    List<Map<String, Object>> borrowTrend(@Param("granularity") String granularity,
                                          @Param("start") LocalDate start,
                                          @Param("end") LocalDate end);

    @Select("""
            <script>
            select b.category as category, count(*) as cnt
            from borrow_record br
            join book b on br.book_id = b.id
            where 1=1
            <if test="start != null">and br.borrow_time &gt;= #{start}</if>
            <if test="end != null">and br.borrow_time &lt;= #{end}</if>
            group by b.category
            order by cnt desc
            </script>
            """)
    List<Map<String, Object>> categoryShare(@Param("start") LocalDate start,
                                            @Param("end") LocalDate end);

    @Select("""
            <script>
            select br.book_id as bookId, bk.title as title, count(*) as cnt
            from borrow_record br
            join book bk on br.book_id = bk.id
            where 1=1
            <if test="start != null">and br.borrow_time &gt;= #{start}</if>
            <if test="end != null">and br.borrow_time &lt;= #{end}</if>
            group by br.book_id, bk.title
            order by cnt desc
            <if test="limit != null">limit #{limit}</if>
            </script>
            """)
    List<Map<String, Object>> topBooks(@Param("start") LocalDate start,
                                       @Param("end") LocalDate end,
                                       @Param("limit") Integer limit);

    @Select("""
            <script>
            select bucket, count(*) as cnt
            from (
                select reader_id,
                       count(*) as borrow_cnt,
                       case
                           when count(*) = 1 then '1次'
                           when count(*) between 2 and 3 then '2-3次'
                           when count(*) between 4 and 6 then '4-6次'
                           else '7次及以上'
                       end as bucket
                from borrow_record
                where 1=1
                <if test="start != null">and borrow_time &gt;= #{start}</if>
                <if test="end != null">and borrow_time &lt;= #{end}</if>
                group by reader_id
            ) t
            group by bucket
            order by
                case bucket
                    when '1次' then 1
                    when '2-3次' then 2
                    when '4-6次' then 3
                    else 4
                end
            </script>
            """)
    List<Map<String, Object>> readerFrequency(@Param("start") LocalDate start,
                                              @Param("end") LocalDate end);

    @Select("""
            <script>
            select status_label as status, count(*) as cnt from (
                select case when status = 'RETURNED' then 'RETURNED' else 'UNRETURNED' end as status_label
                from borrow_record
                where 1=1
                <if test="start != null">and borrow_time &gt;= #{start}</if>
                <if test="end != null">and borrow_time &lt;= #{end}</if>
            ) t
            group by status_label
            </script>
            """)
    List<Map<String, Object>> statusDistribution(@Param("start") LocalDate start,
                                                 @Param("end") LocalDate end);
}
