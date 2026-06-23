package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.CommentReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentReportMapper {

    int insertReport(CommentReport report);

    CommentReport findById(@Param("id") Long id);

    List<CommentReport> findReports(@Param("status") Short status,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    long countReports(@Param("status") Short status);

    int updateReportStatus(@Param("id") Long id,
                           @Param("status") Short status,
                           @Param("handledBy") Long handledBy);

    int countByCommentIdAndReporter(@Param("commentId") Long commentId,
                                    @Param("reporterId") Long reporterId);
}
