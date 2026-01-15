package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    
    /**
     * 根据邀请码查找群组
     */
    Optional<Group> findByInvitationCode(String invitationCode);

    /**
     * 查找用户创建的群组 (按创建时间降序)
     */
    List<Group> findByOwnerIdOrderByCreatedAtDesc(String ownerId);
}
