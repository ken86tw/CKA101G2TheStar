package com.thestar.restaurant.service;

import java.sql.Time;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thestar.restaurant.entity.BusinessHoursVO;
import com.thestar.restaurant.repository.BusinessHoursRepository;

@Service
public class BusinessHoursService {

    @Autowired
    BusinessHoursRepository repository;

    @Autowired
    private SessionFactory sessionFactory;

    public void addBusinessHours(BusinessHoursVO businessHoursVO) {
        // 防呆：如果新增時前端沒有帶狀態值，預設給予 true (上架)
        if (businessHoursVO.getIsAvailable() == null) {
            businessHoursVO.setIsAvailable(true);
        }
        repository.save(businessHoursVO);
    }

    public void updateBusinessHours(BusinessHoursVO businessHoursVO) {
        repository.save(businessHoursVO);
    }

    // === 新增：控制上下架狀態的方法 ===
    @Transactional
    public void updateShelfStatus(Integer sessionId, Boolean status) {
        Optional<BusinessHoursVO> optional = repository.findById(sessionId);
        if (optional.isPresent()) {
            BusinessHoursVO bhVO = optional.get();
            bhVO.setIsAvailable(status);
            repository.save(bhVO); // 更新狀態
        }
    }

    public void deleteBusinessHours(Integer sessionId) {
        if (repository.existsById(sessionId))
            repository.deleteById(sessionId);
    }

    public BusinessHoursVO getOneBusinessHours(Integer sessionId) {
        Optional<BusinessHoursVO> optional = repository.findById(sessionId);
        return optional.orElse(null);
    }

    public List<BusinessHoursVO> getAll() {
        return repository.findAllOrderByStartTime();
    }

    // 查詢目前正在營業的時段
    public List<BusinessHoursVO> getActiveSession(Time currentTime) {
        return repository.findActiveSession(currentTime);
    }
    
    public boolean isTimeSlotDuplicate(java.sql.Time startTime, java.sql.Time endTime) {
        // 只要算出來的筆數大於 0，就代表重複了！
        return repository.countByStartTimeAndEndTime(startTime, endTime) > 0;
    }
}