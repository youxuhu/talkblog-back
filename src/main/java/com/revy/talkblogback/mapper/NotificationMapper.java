package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    List<Notification> findByUserId(@Param("userId") Long userId,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    long countByUserId(@Param("userId") Long userId);

    int countUnreadByUserId(@Param("userId") Long userId);

    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    int markAllAsRead(@Param("userId") Long userId);

    int insert(Notification notification);
}
