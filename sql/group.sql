CREATE TABLE `group` (
                         `id` varchar(255) NOT NULL,
                         `name` varchar(255) DEFAULT NULL,
                         `owner_id` varchar(255) DEFAULT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_group` (
                              `user_id` varchar(255) NOT NULL,
                              `group_id` varchar(255) NOT NULL,
                              `role` varchar(255) DEFAULT NULL,
                              PRIMARY KEY (`user_id`,`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;