CREATE TABLE `users`
(
    `id`           varchar(255) NOT NULL,
    `avatar_url`   varchar(255) DEFAULT NULL,
    `created_at`   datetime(6) DEFAULT NULL,
    `display_name` varchar(255) DEFAULT NULL,
    `email`        varchar(255) DEFAULT NULL,
    `password`     varchar(255) DEFAULT NULL,
    `phone_number` varchar(255) DEFAULT NULL,
    `status`       varchar(255) DEFAULT NULL,
    `updated_at`   datetime(6) DEFAULT NULL,
    `username`     varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `friendships`
(
    `id`           varchar(255) NOT NULL,
    `accepted_at`  datetime(6) DEFAULT NULL,
    `requested_at` datetime(6) NOT NULL,
    `status`       varchar(255) NOT NULL,
    `friend_id`    varchar(255) NOT NULL,
    `user_id`      varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `conversation`
(
    `id`          varchar(255) NOT NULL,
    `created_at`  datetime(6) NOT NULL,
    `created_by`  varchar(255) NOT NULL,
    `is_group`    bit(1)       NOT NULL,
    `is_archived` bit(1)       NOT NULL,
    `name`        varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `message`
(
    `id`              varchar(255) NOT NULL,
    `content`         varchar(255) DEFAULT NULL,
    `created_at`      datetime(6) DEFAULT NULL,
    `is_edited`       bit(1)       NOT NULL,
    `message_type`    enum('FILE','IMAGE','TEXT','VIDEO') DEFAULT NULL,
    `conversation_id` varchar(255) NOT NULL,
    `reply_to`        varchar(255) DEFAULT NULL,
    `sender_id`       varchar(255) NOT NULL,
    `is_seen`         BOOLEAN DEFAULT FALSE,
    `is_recalled`     BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (`id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `conversation_members`
(
    `id`              varchar(255) NOT NULL,
    `joined_at`       datetime(6) NOT NULL,
    `role`            varchar(20)  NOT NULL DEFAULT 'member',
    `conversation_id` varchar(255)          DEFAULT NULL,
    `user_id`         varchar(255)          DEFAULT NULL,
    PRIMARY KEY (`id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `message_status`
(
    `id`         varchar(255) NOT NULL,
    `created_at` datetime(6) NOT NULL,
    `status`     varchar(255) NOT NULL,
    `updated_at` datetime(6) NOT NULL,
    `message_id` varchar(255) NOT NULL,
    `user_id`    varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `attachments`
(
    `id`         varchar(255) NOT NULL,
    `file_size`  bigint       NOT NULL,
    `file_type`  varchar(255) NOT NULL,
    `file_url`   varchar(255) NOT NULL,
    `message_id` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `notifications`
(
    `id`         varchar(255) NOT NULL,
    `content`    varchar(255) NOT NULL,
    `created_at` datetime(6) NOT NULL,
    `extra_data` varchar(255) DEFAULT NULL,
    `is_read`    bit(1)       NOT NULL,
    `read_at`    datetime(6) DEFAULT NULL,
    `type`       varchar(255) NOT NULL,
    `user_id`    varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

-- Posts table
CREATE TABLE `posts`
(
    `id`         bigint       NOT NULL AUTO_INCREMENT,
    `content`    text         NOT NULL,
    `visibility` enum('PUBLIC','PRIVATE','FRIENDS') NOT NULL DEFAULT 'PUBLIC',
    `created_at` datetime(6)  NOT NULL,
    `updated_at` datetime(6)  DEFAULT NULL,
    `deleted_at` datetime(6)  DEFAULT NULL,
    `user_id`    varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_posts_user` (`user_id`),
    CONSTRAINT `fk_posts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

-- Post comments table
CREATE TABLE `post_comments`
(
    `id`               bigint       NOT NULL AUTO_INCREMENT,
    `content`          text         NOT NULL,
    `created_at`       datetime(6)  NOT NULL,
    `deleted_at`       datetime(6)  DEFAULT NULL,
    `post_id`          bigint       NOT NULL,
    `user_id`          varchar(255) NOT NULL,
    `parent_comment_id` bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_post_comments_post` (`post_id`),
    KEY `fk_post_comments_user` (`user_id`),
    KEY `fk_post_comments_parent` (`parent_comment_id`),
    CONSTRAINT `fk_post_comments_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
    CONSTRAINT `fk_post_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_post_comments_parent` FOREIGN KEY (`parent_comment_id`) REFERENCES `post_comments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

-- Post reactions table
CREATE TABLE `post_reactions`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `reaction_type` enum('LIKE','LOVE','HAHA','WOW','SAD','ANGRY') NOT NULL,
    `created_at`    datetime(6)  NOT NULL,
    `post_id`       bigint       NOT NULL,
    `user_id`       varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user_reaction` (`post_id`, `user_id`),
    KEY `fk_post_reactions_post` (`post_id`),
    KEY `fk_post_reactions_user` (`user_id`),
    CONSTRAINT `fk_post_reactions_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
    CONSTRAINT `fk_post_reactions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

-- Post media table
CREATE TABLE `post_media`
(
    `id`        bigint       NOT NULL AUTO_INCREMENT,
    `media_url` varchar(255) NOT NULL,
    `media_type` varchar(255) NOT NULL,
    `post_id`   bigint       NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_post_media_post` (`post_id`),
    CONSTRAINT `fk_post_media_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci