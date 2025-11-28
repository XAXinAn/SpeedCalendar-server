package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, String> {
    Optional<Group> findByInvitationCode(String invitationCode);
}
