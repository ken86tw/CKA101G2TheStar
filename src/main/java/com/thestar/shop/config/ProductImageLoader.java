package com.thestar.shop.config;

import com.thestar.shop.repository.ProductImageRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Comparator;

@Component
public class ProductImageLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductImageLoader.class);

    @Autowired
    DataSource dataSource;

    @Autowired
    ProductImageRepository productImageRepository;

    @Override
    public void run(String... args) throws Exception {

        if (productImageRepository.count() > 0) {
            log.info("商品圖片已存在，跳過載入。");
            return;
        }

        String insert = "INSERT INTO PRODUCT_IMAGE (PRODUCT_ID, PRODUCT_IMAGE, IS_COVER) VALUES (?, ?, ?)";
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // classpath* 前綴：JAR 內外都能讀
        Resource[] resources = resolver.getResources("classpath*:/static/images/products/*/*");
        if (resources.length == 0) {
            log.warn("找不到任何商品圖片資源，跳過載入。");
            return;
        }

        // 依路徑排序，確保封面是可預期的那一張
        Arrays.sort(resources, Comparator.comparing(r -> {
            try {
                return r.getURL().getPath();
            } catch (Exception e) {
                return "";
            }
        }));

        int loaded = 0;
        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt = con.prepareStatement(insert)) {

            Integer lastProductId = null;

            for (Resource res : resources) {
                String path = res.getURL().getPath();
                String[] parts = path.split("/");

                Integer productId;
                try {
                    productId = Integer.valueOf(parts[parts.length - 2]);   // 倒數第二段 = 商品ID資料夾
                } catch (NumberFormatException e) {
                    log.warn("忽略非數字資料夾：{}", path);
                    continue;
                }

                boolean isFirst = !productId.equals(lastProductId);

                try (InputStream fin = res.getInputStream()) {
                    pstmt.setInt(1, productId);
                    pstmt.setBinaryStream(2, fin);
                    pstmt.setInt(3, isFirst ? 1 : 0);
                    pstmt.executeUpdate();
                    loaded++;
                } catch (Exception e) {
                    log.error("圖片載入失敗：{}", path, e);   // 單筆失敗不中斷整批
                }

                lastProductId = productId;
            }
        }

        log.info("商品圖片載入完成，共 {} 張", loaded);
    }
}