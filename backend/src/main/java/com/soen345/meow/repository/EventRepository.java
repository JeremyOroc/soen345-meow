package com.soen345.meow.repository;

import com.soen345.meow.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByStatus(String status);

    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE'" +
           " AND (:category IS NULL OR e.category = :category)" +
           " AND (:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%')))" +
           " AND (:startDate IS NULL OR e.eventDatetime >= :startDate)" +
           " AND (:endDate IS NULL OR e.eventDatetime <= :endDate)")
    List<Event> findActiveWithFilters(@Param("category") String category,
                                      @Param("location") String location,
                                      @Param("startDate") String startDate,
                                      @Param("endDate") String endDate);
}
