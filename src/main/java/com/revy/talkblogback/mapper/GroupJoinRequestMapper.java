package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.GroupJoinRequest;
import com.revy.talkblogback.pojo.response.GroupJoinRequestResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupJoinRequestMapper {

    int insertRequest(GroupJoinRequest request);

    GroupJoinRequest findRequestById(@Param("id") Long id);

    GroupJoinRequest findPendingRequest(@Param("groupId") Long groupId, @Param("userId") Long userId);

    List<GroupJoinRequestResponse> findRequestsByGroupId(@Param("groupId") Long groupId);

    List<GroupJoinRequestResponse> findRequestsByUserId(@Param("userId") Long userId);

    int updateRequestStatus(@Param("id") Long id, @Param("status") Short status);

    GroupJoinRequest findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    int updateRequest(@Param("id") Long id, @Param("status") Short status, @Param("reason") String reason);
}
