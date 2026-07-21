package com.thestar.room.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.repository.RoomInventoryRepository;
import com.thestar.room.repository.RoomRepository;
import com.thestar.room.repository.RoomTypePhotoRepository;
import com.thestar.room.repository.RoomTypeRepository;
import com.thestar.stayrecord.repository.StayRecordRepository;

@Service // 標記為 Service 物件，讓 Spring 管理
@Transactional
public class RoomTypeService {

	// 自動注入
	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private RoomTypeRepository repository;

	@Autowired
	private RoomTypePhotoRepository photoRepository;

	@Autowired
	private StayRecordRepository stayRecordRepository;

	@Autowired
	private RoomInventoryRepository roomInventoryRepository;

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

	// 房型修改前驗證
	private void validateUpdate(RoomTypeVO updatedRoomType) {
		// 1. 先取得「資料庫中目前的版本」
		RoomTypeVO currentRoomType = getOneRoomType(updatedRoomType.getRoomTypeId());

		// 2. 判斷邏輯：只有在「原本就是啟用」且「現在送進來的還是啟用」時，才執行嚴格檢查
		// 如果「原本是啟用」但「現在送進來的是未啟用」，我們應該放行，讓它去修改成未啟用
		boolean isStillEnabled = Boolean.TRUE.equals(currentRoomType.getRoomTypeStatus())
				&& Boolean.TRUE.equals(updatedRoomType.getRoomTypeStatus());

		if (isStillEnabled) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "更新失敗：該房型目前為「啟用」狀態，若需修改資訊，請先手動變更為「未啟用」。");
		}

		// 3. 檢查：已有房間實體 (這部分維持現狀)
		long roomCount = roomRepository.countByRoomTypeId(updatedRoomType.getRoomTypeId());
		if (roomCount > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "更新失敗：該房型下尚有房間，請先移除房間後再修改！");
		}

		// 4. 檢查：已有訂單/住宿紀錄 (這部分維持現狀)
		int recordCount = stayRecordRepository.countActiveByRoomTypeId(updatedRoomType.getRoomTypeId());
		if (recordCount > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "更新失敗：該房型已有相關住宿訂單紀錄，不可修改！");
		}
	}

	// 更新房型
	@Transactional
	public void updateRoomType(RoomTypeVO roomType) {
		// 1. 取得資料庫原始資料
		RoomTypeVO existingRoom = getOneRoomType(roomType.getRoomTypeId());

		// 2. 檢查文字/狀態欄位是否有被更動
		boolean isTextFieldsChanged = !existingRoom.getRoomTypeName().equals(roomType.getRoomTypeName())
				|| !objectsEqual(existingRoom.getRoomTypeContent(), roomType.getRoomTypeContent())
				|| !existingRoom.getRoomTypePrice().equals(roomType.getRoomTypePrice())
				|| !existingRoom.getRoomTypeStatus().equals(roomType.getRoomTypeStatus())
				|| !existingRoom.getCapacity().equals(roomType.getCapacity())
				|| !objectsEqual(existingRoom.getAmenities(), roomType.getAmenities());

		// 3. 只有當「文字或狀態欄位」被修改時，才執行嚴格的 validateUpdate 檢查！
		// 如果只是單純上傳圖片、刪除圖片，而文字沒變，就不會被嚴格檢查擋下
		if (isTextFieldsChanged) {
			validateUpdate(roomType);
		}

		// 4. 強制重新統計該房型的「真實房間實體數量」
		int actualCount = (int) roomRepository.countByRoomTypeId(roomType.getRoomTypeId());

		// 5. 更新屬性
		existingRoom.setRoomTypeName(roomType.getRoomTypeName());
		existingRoom.setRoomTypeAmount(actualCount);
		existingRoom.setRoomTypePrice(roomType.getRoomTypePrice());
		existingRoom.setRoomTypeStatus(roomType.getRoomTypeStatus());
		existingRoom.setRoomTypeContent(roomType.getRoomTypeContent());
		existingRoom.setCapacity(roomType.getCapacity());
		existingRoom.setAmenities(roomType.getAmenities());

		repository.save(existingRoom);
	}

	// 刪除房型
	public void deleteRoomType(Integer id) {
		// 先取得該房型物件，若找不到直接拋出 404，並將結果存入 roomType 變數
		RoomTypeVO roomType = repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "刪除失敗：找不到該房型 (ID: " + id + ")"));

		// 檢查：如果房型目前是「啟用」狀態，禁止刪除
		if (Boolean.TRUE.equals(roomType.getRoomTypeStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無法刪除：該房型目前為「啟用」狀態，請先將其變更為「未啟用」後再執行刪除！");
		}

		// 檢查訂單紀錄 (防止刪除有歷史紀錄的房型)
		if (stayRecordRepository.countActiveByRoomTypeId(id) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無法刪除：該房型已有相關訂單紀錄！");
		}
		
		// 傳入 id 與當前日期，確保檢查的是未來庫存
		if (roomInventoryRepository.existsByRoomTypeIdAndDate(id, java.time.LocalDate.now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無法刪除：該房型已有未來庫存排程，請先清空庫存！");
		}

		// 檢查房間實體
		if (roomRepository.countByRoomTypeId(id) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無法刪除：該房型下尚有房間，請先處理關聯房間！");
		}

		// 刪除所有相關聯的照片
		photoRepository.deleteByRoomTypeVORoomTypeId(id);

		// 刪除房型本身
		repository.deleteById(id);

	}

	// 輔助方法：用來安全比對可能為 null 的字串
	private boolean objectsEqual(String a, String b) {
		return (a == null ? "" : a).equals(b == null ? "" : b);
	}

}
