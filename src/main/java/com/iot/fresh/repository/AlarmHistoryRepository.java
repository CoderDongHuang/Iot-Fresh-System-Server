package com.iot.fresh.repository;

import com.iot.fresh.entity.AlarmHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmHistoryRepository extends JpaRepository<AlarmHistory, Long> {

    /**
     * 根据报警ID查找处理记录
     * @param alarmId 报警ID
     * @return 处理记录列表
     */
    List<AlarmHistory> findByAlarmIdOrderByTimestampDesc(Long alarmId);

    /**
     * 根据报警ID和操作类型查找处理记录
     * @param alarmId 报警ID
     * @param action 操作类型
     * @return 处理记录列表
     */
    List<AlarmHistory> findByAlarmIdAndActionOrderByTimestampDesc(Long alarmId, String action);

    /**
     * 删除指定报警的所有处理记录
     * @param alarmId 报警ID
     */
    void deleteByAlarmId(Long alarmId);

    /**
     * 统计指定报警的处理记录数量
     * @param alarmId 报警ID
     * @return 记录数量
     */
    @Query("SELECT COUNT(h) FROM AlarmHistory h WHERE h.alarmId = :alarmId")
    long countByAlarmId(@Param("alarmId") Long alarmId);
}