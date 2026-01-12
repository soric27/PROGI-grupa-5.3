package com.autoservis.repositories;

import com.autoservis.models.Obrazac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObrazacRepository extends JpaRepository<Obrazac, Long> {
}
