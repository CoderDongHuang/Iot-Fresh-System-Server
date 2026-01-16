package com.iot.fresh.repository;

import com.iot.fresh.entity.DeviceData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeviceDataRepository extends JpaRepository<DeviceData, Long> {
    List<DeviceData> findByVid(String vid);
    
    @Query("SELECT d FROM DeviceData d WHERE d.vid = :vid AND d.createdAt BETWEEN :startTime AND :endTime ORDER BY d.createdAt DESC")
    List<DeviceData> findByVidAndTimeRange(@Param("vid") String vid, 
                                          @Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);
    
    List<DeviceData> findByVidOrderByCreatedAtDesc(String vid, Pageable pageable);
    
    DeviceData findTopByVidOrderByCreatedAtDesc(String vid);
    
    @Query("SELECT d FROM DeviceData d WHERE d.vid = :vid AND d.createdAt BETWEEN :startTime AND :endTime ORDER BY d.createdAt DESC")
    org.springframework.data.domain.Page<DeviceData> findByVidAndTimeRangeWithPagination(
            @Param("vid") String vid, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime, 
            Pageable pageable);
    
    @Query("SELECT d FROM DeviceData d WHERE d.createdAt BETWEEN :startTime AND :endTime ORDER BY d.createdAt DESC")
    org.springframework.data.domain.Page<DeviceData> findByTimeRangeWithPagination(
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime, 
            Pageable pageable);
    
    @Query("SELECT d FROM DeviceData d WHERE d.createdAt BETWEEN :startTime AND :endTime ORDER BY d.createdAt DESC")
    List<DeviceData> findByTimeRangeWithNoPagination(@Param("startTime") LocalDateTime startTime, 
                                                    @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT d FROM DeviceData d WHERE d.vid = :vid AND d.createdAt BETWEEN :startTime AND :endTime ORDER BY d.createdAt DESC")
    Page<DeviceData> findByVidAndCreatedAtBetween(@Param("vid") String vid, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);

    @Query("SELECT DISTINCT d.vid FROM DeviceData d")
    List<String> findDistinctVids();
    
    void deleteByVid(String vid);
}