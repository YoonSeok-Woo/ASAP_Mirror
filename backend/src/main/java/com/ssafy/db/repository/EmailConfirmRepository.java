package com.ssafy.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ssafy.db.entity.EmailConfirm;

@Repository
public interface EmailConfirmRepository extends JpaRepository<EmailConfirm, Integer>, EmailConfirmRepositoryCustom{
}
