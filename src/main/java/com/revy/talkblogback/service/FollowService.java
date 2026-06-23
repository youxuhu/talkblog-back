package com.revy.talkblogback.service;

import com.revy.talkblogback.mapper.FollowMapper;
import com.revy.talkblogback.pojo.Follow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class FollowService {

    private final FollowMapper followMapper;

    public FollowService(FollowMapper followMapper) {
        this.followMapper = followMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        Follow existing = followMapper.findByFollowerAndFollowee(followerId, followeeId);
        if (existing != null) {
            followMapper.delete(followerId, followeeId);
            return false;
        } else {
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            followMapper.insert(follow);
            return true;
        }
    }

    public boolean isFollowing(Long followerId, Long followeeId) {
        return followMapper.findByFollowerAndFollowee(followerId, followeeId) != null;
    }

    public long countFollowers(Long userId) {
        return followMapper.countFollowers(userId);
    }

    public long countFollowees(Long userId) {
        return followMapper.countFollowees(userId);
    }

    public List<Map<String, Object>> getFollowers(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return followMapper.findFollowers(userId, offset, size);
    }

    public List<Map<String, Object>> getFollowees(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return followMapper.findFollowees(userId, offset, size);
    }
}
