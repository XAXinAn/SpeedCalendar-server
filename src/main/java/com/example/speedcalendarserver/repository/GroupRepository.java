package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, String> {
}
