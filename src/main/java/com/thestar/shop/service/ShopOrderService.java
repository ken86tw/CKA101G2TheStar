package com.thestar.shop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thestar.shop.entity.ShopOrderVO;
import com.thestar.shop.repository.ShopOrderRepository;

@Service
public class ShopOrderService {

    @Autowired
    ShopOrderRepository repository;
    @Autowired
    com.thestar.member.service.MemberNotifyService memberNotifySvc;

    public void updateShopOrder(ShopOrderVO shopOrderVO) {
        repository.save(shopOrderVO);
    }

    public void deleteShopOrder(Integer shopOrderId) {
        if (repository.existsById(shopOrderId))
            repository.deleteByShopOrderId(shopOrderId);
    }

    public ShopOrderVO getOneShopOrder(Integer shopOrderId) {
        Optional<ShopOrderVO> optional = repository.findById(shopOrderId);
        return optional.orElse(null);
    }

    public List<ShopOrderVO> getAll() {
        return repository.findAll();
    }

    public List<ShopOrderVO> getByMemberId(Integer memberId) {
        return repository.findByMemberId(memberId);
    }
    
    public void addShopOrder(ShopOrderVO shopOrderVO) {
        repository.save(shopOrderVO);
        // 訂單建立通知
        memberNotifySvc.createNotification(shopOrderVO.getMemberId(),
            "您的購物訂單已成立，訂單編號：" + shopOrderVO.getShopOrderId() + "，請盡快完成付款！");
    }

    public void updateShopOrderWithPayment(ShopOrderVO shopOrderVO, Integer orderId) {
        repository.save(shopOrderVO);
        // 付款成功通知
        memberNotifySvc.createNotification(shopOrderVO.getMemberId(),
            "購物訂單編號 " + orderId + " 付款成功，感謝您的購買！");
    }
    
    public void cancelShopOrder(ShopOrderVO shopOrderVO) {
        shopOrderVO.setShopOrderStatus((byte) 3);
        repository.save(shopOrderVO);
        // 逾時取消通知
        memberNotifySvc.createNotification(shopOrderVO.getMemberId(),
            "購物訂單編號 " + shopOrderVO.getShopOrderId() + " 因逾時未付款已自動取消。");
    }
    
    public void cancelShopOrderManually(ShopOrderVO shopOrderVO) {
        repository.save(shopOrderVO);
        // 手動取消通知
        memberNotifySvc.createNotification(shopOrderVO.getMemberId(),
            "您的購物訂單編號 " + shopOrderVO.getShopOrderId() + " 已由客服取消，如有疑問請聯繫我們。");
    }
    
    public void deliverShopOrder(ShopOrderVO shopOrderVO) {
        repository.save(shopOrderVO);
        // 已送達通知
        memberNotifySvc.createNotification(shopOrderVO.getMemberId(),
            "您的購物訂單編號 " + shopOrderVO.getShopOrderId() + " 已送達，感謝您的購買！如需評論請至我的訂單頁面。");
    }
    
    public void shipShopOrder(ShopOrderVO shopOrderVO) {
        repository.save(shopOrderVO);
        // 出貨通知
        memberNotifySvc.createNotification(shopOrderVO.getMemberId(),
            "您的購物訂單編號 " + shopOrderVO.getShopOrderId() + " 已出貨，請耐心等候！");
    }
}