package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.Route;
import com.ccalarce.siglof.model.enums.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByStatus(RouteStatus status);
}
