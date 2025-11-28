package com.example.speedcalendarserver.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "`group`")
public class Group {
    @Id
    private String id;
    private String name;
    private String ownerId;
}
