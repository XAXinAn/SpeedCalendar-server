package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.UserGroup;
import com.example.speedcalendarserver.entity.UserGroupKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, UserGroupKey> {
    
    /**
     * 查找用户加入的群组 (按加入时间降序)
     */
    List<UserGroup> findByUserIdOrderByJoinedAtDesc(String userId);

    /**
     * 查找用户加入的所有群组 (无排序，兼容旧代码)
     */
    List<UserGroup> findByUserId(String userId);
    
    UserGroup findByUserIdAndGroupId(String userId, String groupId);
    
    List<UserGroup> findByGroupId(String groupId);
    
    /**
     * 统计群组成员数量
     */
    int countByGroupId(String groupId);
}
