package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.ChatGroup;
import com.revy.talkblogback.pojo.GroupMember;
import com.revy.talkblogback.pojo.GroupBanRecord;
import com.revy.talkblogback.pojo.response.ChatGroupResponse;
import com.revy.talkblogback.pojo.response.GroupMemberResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatGroupMapper {

    int insertGroup(ChatGroup group);

    ChatGroup findGroupById(@Param("groupId") Long groupId);

    List<ChatGroupResponse> findGroupsByUserId(@Param("userId") Long userId);

    List<ChatGroupResponse> findPublicGroups(@Param("userId") Long userId);

    List<ChatGroupResponse> searchGroups(@Param("keyword") String keyword, @Param("userId") Long userId);

    List<ChatGroupResponse> pageAllGroups(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    long countAllGroups(@Param("keyword") String keyword);

    int deleteGroup(@Param("groupId") Long groupId);

    int insertMember(GroupMember member);

    int removeMember(@Param("groupId") Long groupId, @Param("userId") Long userId);

    GroupMember findMember(@Param("groupId") Long groupId, @Param("userId") Long userId);

    int updateMemberRole(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("role") Short role);

    int countMembers(@Param("groupId") Long groupId);

    int insertBanRecord(GroupBanRecord record);

    int isUserBanned(@Param("groupId") Long groupId, @Param("userId") Long userId);

    ChatGroupResponse findGroupByIdWithMemberCount(@Param("groupId") Long groupId);

    List<GroupMemberResponse> findMembersByGroupId(@Param("groupId") Long groupId);

    long countMessagesByGroupId(@Param("groupId") Long groupId);

    int deleteAllMembers(@Param("groupId") Long groupId);

    int deleteAllMessages(@Param("groupId") Long groupId);

    int deleteAllBanRecords(@Param("groupId") Long groupId);

    int updateGroupName(@Param("groupId") Long groupId, @Param("groupName") String groupName);

    int updateGroupSettings(@Param("groupId") Long groupId, @Param("groupType") Short groupType, @Param("isPublic") Short isPublic);
}
