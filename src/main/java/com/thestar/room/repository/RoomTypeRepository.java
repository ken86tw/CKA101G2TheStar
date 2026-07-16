package com.thestar.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.thestar.room.entity.RoomTypeVO;

public interface RoomTypeRepository extends JpaRepository <RoomTypeVO, Integer>{

	@Query("SELECT SUM(r.roomTypeAmount) FROM RoomTypeVO r")
    Integer sumAllRoomAmounts();


}
