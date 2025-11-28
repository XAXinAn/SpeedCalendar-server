package com.example.speedcalendarserver.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "user_group")
@IdClass(UserGroup.class)
public class UserGroup implements Serializable {
    @Id
    private String userId;
    @Id
    private String groupId;
    private String role;
}
