package com.autoservis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoservis.models.ServisInfo;

public interface ServisInfoRepository extends JpaRepository<ServisInfo, Long> {
}
