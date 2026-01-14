package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.Route;
import com.ccalarce.siglof.model.enums.RouteStatus;
import com.ccalarce.siglof.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;List<Route>findByStatus(RouteStatus status);

Optional<Route>findByDriverAndStatus(User driver,RouteStatus status);}
