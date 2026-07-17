package com.thestar.room.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.entity.RoomVO;
import com.thestar.room.repository.RoomRepository;
import com.thestar.stayrecord.entity.StayRecordVO;
import com.thestar.stayrecord.repository.StayRecordRepository;

@Service
@Transactional
public class RoomService {

	@Autowired
	private RoomRepository repository;

	@Autowired
	private RoomTypeService roomTypeService;

	@Autowired
	private StayRecordRepository stayRecordRepository;

	// 查詢所有房間
	public List<RoomVO> findAll() {
		// 先從資料庫撈出所有資料，並指派給變數 list
		List<RoomVO> list = repository.findAll();

		// 確定 list 已經建立，再進行搜尋房型編號
		for (RoomVO room : list) {
			RoomTypeVO type = roomTypeService.getOneRoomType(room.getRoomTypeId());
			if (type != null) {
				room.setRoomTypeName(type.getRoomTypeName());
			}
		}
		return list;
	}

	// 查詢單一房間
	public RoomVO findById(Integer id) {
		RoomVO room = repository.findById(id)
				// 找不到對應id時，回傳錯誤訊息
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到對應ID的房間"));

		// 查出對應的房型名稱並補入
		RoomTypeVO type = roomTypeService.getOneRoomType(room.getRoomTypeId());
		if (type != null) {
			room.setRoomTypeName(type.getRoomTypeName());
		}
		return room;
	}

	// 新增或更新房間
	public RoomVO save(RoomVO room) {
		return repository.save(room);
	}

	// 刪除房間
	public void deleteById(Integer id) {
		// 1. 查詢所有屬於該 roomId 的住宿紀錄
		List<StayRecordVO> records = stayRecordRepository.findByRoomId(id);

		// 2. 如果找得到的紀錄不為空 (長度大於 0)，代表該房間有歷史紀錄，禁止刪除
		if (records != null && !records.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "刪除失敗：該房間已有相關住宿紀錄，為保留完整帳務資料，禁止刪除。");
		}

		// 3. 若無關聯紀錄，才執行刪除
		repository.deleteById(id);
	}

	// 檢查單一房型ID是否存在
	public boolean existsById(Integer roomId) {
		// 這裡直接呼叫 repository 內建的方法
		return repository.existsById(roomId);
	}

	// 目前在 room 資料表中，有多少間房間屬於指定的房型 (由 roomTypeId 指定)
	// 根據房型編號，統計資料庫中該房型目前已配置的房間總數。
	public long countRoomsByTypeId(Integer roomTypeId) {
		return (int) repository.countByRoomTypeId(roomTypeId);
	}

	public int countBookedRoomsByTypeId(Integer roomTypeId) {
		// 呼叫剛剛新增的 Repository 方法
		return stayRecordRepository.countActiveByRoomTypeId(roomTypeId);
	}
}
