package com.thestar.room.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.repository.RoomRepository;
import com.thestar.room.repository.RoomTypePhotoRepository;
import com.thestar.room.repository.RoomTypeRepository;

@Service // 標記為 Service 物件，讓 Spring 管理
@Transactional
public class RoomTypeService {

	@Autowired
	private RoomRepository roomRepository;

	@Autowired // 自動注入
	private RoomTypeRepository repository;

	@Autowired
	private RoomTypePhotoRepository photoRepository;

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
		// 1. 強制將初始數量設為 0
		// 因為剛新增的房型一定還沒有對應的實體房間
		roomType.setRoomTypeAmount(0);

		// 2. 直接儲存，不需要再檢查 MAX_HOTEL_CAPACITY
		// 因為房型數量增加並不代表房間實體數量增加
		return repository.save(roomType);
	}

	// 更新房型
	@Transactional
	public void updateRoomType(RoomTypeVO roomType) {
		// 1. 取得資料庫原始資料
		RoomTypeVO existingRoom = getOneRoomType(roomType.getRoomTypeId());

		// 2. [重要] 強制重新統計該房型的「真實房間實體數量」
		// 這樣就不需要再擔心前端傳過來的數量與資料庫不符，也不需要手動檢查數量上限了
		int actualCount = (int) roomRepository.countByRoomTypeId(roomType.getRoomTypeId());

		// 3. 更新屬性 (除了 amount 由系統自動維護外，其餘保持更新)
		existingRoom.setRoomTypeName(roomType.getRoomTypeName());
		existingRoom.setRoomTypeAmount(actualCount); // 強制覆寫為真實統計數量
		existingRoom.setRoomTypePrice(roomType.getRoomTypePrice());
		existingRoom.setRoomTypeStatus(roomType.getRoomTypeStatus());
		existingRoom.setRoomTypeContent(roomType.getRoomTypeContent());

		repository.save(existingRoom);
	}

	// 刪除房型
	public void deleteRoomType(Integer id) {
	    // 1. 先確認該房型是否存在
	    if (!repository.existsById(id)) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "刪除失敗：找不到該房型 (ID: " + id + ")");
	    }

	    // 2. [檢查] 檢查該房型下是否還有關聯的房間實體
	    long count = roomRepository.countByRoomTypeId(id);
	    if (count > 0) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無法刪除：該房型下尚有 " + count + " 間房間，請先處理關聯房間！");
	    }

	    // 3. 刪除所有相關聯的照片
	    photoRepository.deleteByRoomTypeVORoomTypeId(id);

	    // 4. 刪除房型本身
	    repository.deleteById(id);

	}

}
