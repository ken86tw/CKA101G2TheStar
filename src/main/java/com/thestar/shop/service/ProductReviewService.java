package com.thestar.shop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thestar.shop.entity.ProductReviewVO;
import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.repository.ProductReviewRepository;
import com.thestar.shop.repository.ProductsRepository;

@Service
public class ProductReviewService {

	@Autowired
	ProductReviewRepository repository;
	@Autowired
	ProductsRepository productsRepository;

	public void addProductReview(ProductReviewVO productReviewVO) {
		repository.save(productReviewVO);
	}

	public void updateProductReview(ProductReviewVO productReviewVO) {
		repository.save(productReviewVO);
	}

	@Transactional
	public void deleteProductReview(Integer productReviewId) {
		ProductReviewVO review = repository.findById(productReviewId).orElse(null);
		if (review == null)
			return;

		// ① 商品統計
		ProductsVO product = productsRepository.findById(review.getProductId()).orElse(null);
		if (product != null) {
			int count = (product.getProductReviewNumber() == null ? 0 : product.getProductReviewNumber()) - 1;
			int star = (product.getProductTotalStar() == null ? 0 : product.getProductTotalStar())
					- (review.getProductRate() == null ? 0 : review.getProductRate());
			product.setProductReviewNumber(Math.max(0, count)); // 防止變負數
			product.setProductTotalStar(Math.max(0, star));
			productsRepository.save(product);
		}

		// ② 刪除評論（順便改用內建方法，一併解決 #12）
		repository.deleteById(productReviewId);

		// ③ 訂單明細維持 REVIEW_STAT = 1，不讓使用者重新張貼不當內容
	}

	public ProductReviewVO getOneProductReview(Integer productReviewId) {
		Optional<ProductReviewVO> optional = repository.findById(productReviewId);
		return optional.orElse(null);
	}

	public List<ProductReviewVO> getAll() {
		return repository.findAll();
	}

	public List<ProductReviewVO> getByProductId(Integer productId) {
		return repository.findByProductId(productId);
	}

	public List<ProductReviewVO> getByMemberId(Integer memberId) {
		return repository.findByMemberId(memberId);
	}

	public boolean existsByProductOrderItemId(Integer productOrderItemId) {
		return repository.existsByProductOrderItemId(productOrderItemId);
	}

	public void updateAverageRating(ProductsVO product, Byte newRate) {
		int totalStar = (product.getProductTotalStar() == null ? 0 : product.getProductTotalStar()) + newRate;
		int reviewNumber = (product.getProductReviewNumber() == null ? 0 : product.getProductReviewNumber()) + 1;
		product.setProductTotalStar(totalStar);
		product.setProductReviewNumber(reviewNumber);
	}
}