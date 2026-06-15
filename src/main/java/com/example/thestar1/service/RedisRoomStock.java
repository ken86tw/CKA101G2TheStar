package com.example.thestar1.service;


import com.example.thestar1.repository.RoomInventoryRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RedisRoomStock {

    private final StringRedisTemplate redisTemplate;
    private final RoomInventoryRepository roomInventoryRepository;

    public RedisRoomStock(StringRedisTemplate redisTemplate, RoomInventoryRepository roomInventoryRepository) {
        this.redisTemplate = redisTemplate;
        this.roomInventoryRepository = roomInventoryRepository;
    }

    private String roomKey(Integer roomTypeId, LocalDate date) {
        return "room:" + roomTypeId + ":" + date;
    }

    public void initRedisRoom(Integer roomTypeId, LocalDate date) {
        String key = roomKey(roomTypeId, date);
        int room = roomInventoryRepository.checkInventory(roomTypeId, date);

        redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(room));
    }

    public boolean bookRedisRoom(Integer roomTypeId, LocalDate date, int qty) {
        String key = roomKey(roomTypeId, date);
        long result = redisTemplate.opsForValue().decrement(key, qty);

        if (result < 0) {
            redisTemplate.opsForValue().increment(key, qty);
            return false;
        }
        return true;
    }

    // 歸還庫存  已消失就不硬建，留給下次 initRedisRoom 從DB重建
    public void releaseRoom(Integer roomTypeId, LocalDate date, int qty) {
        String key = roomKey(roomTypeId, date);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().increment(key, qty);
        }
    }
}
