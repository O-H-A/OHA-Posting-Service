package com.oha.posting.repository;

import com.oha.posting.entity.Like;
import com.oha.posting.entity.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, LikeId> {
}
