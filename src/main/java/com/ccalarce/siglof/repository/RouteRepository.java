package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.Route;
import com.ccalarce.siglof.model.enums.RouteStatus;
import com.ccalarce.siglof.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
        List<Route> findByStatus(RouteStatus status);

        Integer countByStatus(RouteStatus status);

        Optional<Route> findByDriverAndStatus(User driver, RouteStatus status);

        /**
         * Optimized query with JOIN FETCH to load Route with all relationships in a
         * single query.
         * Prevents N+1 queries by eagerly fetching vehicle, driver, and stock with
         * products.
         */
        @Query("SELECT DISTINCT r FROM Route r " +
                        "LEFT JOIN FETCH r.vehicle " +
                        "LEFT JOIN FETCH r.driver " +
                        "LEFT JOIN FETCH r.stock s " +
                        "LEFT JOIN FETCH s.product " +
                        "WHERE r.driver = :driver AND r.status = :status")
        Optional<Route> findByDriverAndStatusWithDetails(@Param("driver") User driver,
                        @Param("status") RouteStatus status);

        /**
         * Optimized query to find all routes by status with all relationships loaded.
         */
        @Query("SELECT DISTINCT r FROM Route r " +
                        "LEFT JOIN FETCH r.vehicle " +
                        "LEFT JOIN FETCH r.driver " +
                        "LEFT JOIN FETCH r.stock s " +
                        "LEFT JOIN FETCH s.product " +
                        "WHERE r.status = :status")
        List<Route> findByStatusWithDetails(@Param("status") RouteStatus status);

        /**
         * Optimized query to find route by ID with all relationships.
         */
        @Query("SELECT DISTINCT r FROM Route r " +
                        "LEFT JOIN FETCH r.vehicle " +
                        "LEFT JOIN FETCH r.driver " +
                        "LEFT JOIN FETCH r.stock s " +
                        "LEFT JOIN FETCH s.product " +
                        "WHERE r.id = :id")
        Optional<Route> findByIdWithDetails(@Param("id") Long id);
}
