package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserGroupRepository extends JpaRepository<UserGroup, String> {
    List<UserGroup> findByUserId(String userId);
    UserGroup findByUserIdAndGroupId(String userId, String groupId);
}
