package com.archeologist.repository;

import com.archeologist.entity.CodeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeAnalysisRepository extends JpaRepository<CodeAnalysis, Long> { }
