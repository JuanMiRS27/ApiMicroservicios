package com.inventory.reportingservice.repository;

import com.inventory.reportingservice.entity.UserAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccessLogRepository extends JpaRepository<UserAccessLog, Long> {
}
