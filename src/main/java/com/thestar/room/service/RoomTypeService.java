package com.thestar.room.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.repository.RoomTypePhotoRepository;
import com.thestar.room.repository.RoomTypeRepository;

@Service // 標記為 Service 物件，讓 Spring 管理
@Transactional
public class RoomTypeService {

	@Autowired // 自動注入
	private RoomTypeRepository repository;

	@Autowired
	private RoomTypePhotoRepository photoRepository;

	// 限制房型總數
	private static final int MAX_HOTEL_CAPACITY = 50;

	// 查詢所有房型
	public List<RoomTypeVO> getAllRoomTypes() {
		return repository.findAll();
	}

	// 查詢單一房型
	public RoomTypeVO getOneRoomType(Integer id) {
		return repository.findById(id).orElseThrow(); // 找不到對應id時，回傳錯誤訊息
	}

	// 新增房型 (含總量檢查)
	@Transactional
	public RoomTypeVO addRoomType(RoomTypeVO roomType) {
		Integer currentTotal = repository.sumAllRoomAmounts();
		if (currentTotal == null)
			currentTotal = 0;

		if (currentTotal + roomType.getRoomTypeAmount() > MAX_HOTEL_CAPACITY) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "新增失敗：全館總房間數不能超過 " + MAX_HOTEL_CAPACITY + " 間");
		}
		return repository.save(roomType);
	}

	// 更新房型
	@Transactional
	public void updateRoomType(RoomTypeVO roomType) {
		// 1. 取得資料庫原始資料
		RoomTypeVO existingRoom = getOneRoomType(roomType.getRoomTypeId());

		// 2. 計算排除當前房型後的「其他房型」總和
		Integer currentTotal = repository.sumAllRoomAmounts();
		if (currentTotal == null)
			currentTotal = 0;
		int othersTotal = currentTotal - existingRoom.getRoomTypeAmount();

		// 3. 檢查更新後的總和
		if (othersTotal + roomType.getRoomTypeAmount() > MAX_HOTEL_CAPACITY) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "更新失敗：總房間數不能超過 " + MAX_HOTEL_CAPACITY + " 間");
		}

		// 4. 更新屬性並儲存
		existingRoom.setRoomTypeName(roomType.getRoomTypeName());
		existingRoom.setRoomTypeAmount(roomType.getRoomTypeAmount());
		existingRoom.setRoomTypePrice(roomType.getRoomTypePrice());
		existingRoom.setRoomTypeStatus(roomType.getRoomTypeStatus());
		existingRoom.setRoomTypeContent(roomType.getRoomTypeContent());

		repository.save(existingRoom);
	}

	// 刪除房型
	@Transactional
	public void deleteRoomType(Integer id) {
		// 1. 先根據 room_type_id 刪除所有相關聯的照片
		photoRepository.deleteByRoomTypeVORoomTypeId(id);

		// 2. 再刪除房型本身
		repository.deleteById(id);

	}

	// 計算房型總數
	public int getSumOfAmounts() {
		Integer total = repository.sumAllRoomAmounts();
		return (total != null) ? total : 0;
	}
}
