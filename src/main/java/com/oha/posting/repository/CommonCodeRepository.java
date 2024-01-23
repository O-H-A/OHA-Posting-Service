package com.oha.posting.repository;

import com.oha.posting.entity.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommonCodeRepository extends JpaRepository<CommonCode, String> {

    Optional<CommonCode> findByCode(String code);
}
