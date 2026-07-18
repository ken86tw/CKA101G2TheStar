| Email                                                 | 權限（角色） |
| ----------------------------------------------------- | ------ |
| [hr.lin@hotel.com](mailto:hr.lin@hotel.com)           | 人資管理員（僅員工模組）  |
| [it.chen@hotel.com](mailto:it.chen@hotel.com)         | 系統管理員  |
| [room.wang@hotel.com](mailto:room.wang@hotel.com)     | 櫃檯人員   | (處理訂房)
| [food.zhang@hotel.com](mailto:food.zhang@hotel.com)   | 餐廳人員   |
| [product.li@hotel.com](mailto:product.li@hotel.com)   | 商品管理員  |
| [member.wang@hotel.com](mailto:member.wang@hotel.com) | 會員管理員  |
| [content.chen@hotel.com](mailto:content.chen@hotel.com) | 文章管理員  |

## 資料庫初始化

先建立 `thestar` 資料庫並匯入專案原始 schema／seed data，再執行 HR 權限 migration：

```bash
mysql -u root -p thestar < database/migrations/20260717_add_hr_role.sql
```

這支 migration 會建立或啟用 `ROLE_HR`，並將 `hr.lin@hotel.com` 的角色調整為只保留 HR。腳本不依賴固定 ID，可安全重複執行。執行後重新啟動應用程式，並讓該帳號重新登入。
