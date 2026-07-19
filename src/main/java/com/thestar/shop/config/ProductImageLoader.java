package com.thestar.shop.config;

import com.thestar.shop.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;

@Component
public class ProductImageLoader implements CommandLineRunner {

    @Autowired
    DataSource dataSource;

    @Autowired
    ProductImageRepository productImageRepository;

    @Override
    public void run(String... args) throws Exception {
        // 如果已經有圖片就不執行
        if (productImageRepository.count() > 0) {
            System.out.println("商品圖片已存在，跳過載入。");
            return;
        }

        String photosRoot = "src/main/resources/static/images/products";
        String insert = "INSERT INTO PRODUCT_IMAGE (PRODUCT_ID, PRODUCT_IMAGE, IS_COVER) VALUES (?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt = con.prepareStatement(insert)) {

            File rootDir = new File(photosRoot);
            if (!rootDir.exists()) {
                System.out.println("圖片資料夾不存在，跳過載入。");
                return;
            }

            File[] productFolders = rootDir.listFiles(File::isDirectory);
            if (productFolders == null) return;

            for (File folder : productFolders) {
                try {
                    int productId = Integer.parseInt(folder.getName());
                    File[] imageFiles = folder.listFiles();
                    if (imageFiles == null) continue;

                    boolean isFirst = true;
                    for (File f : imageFiles) {
                        try (InputStream fin = new FileInputStream(f)) {
                            pstmt.setInt(1, productId);
                            pstmt.setBinaryStream(2, fin);
                            pstmt.setInt(3, isFirst ? 1 : 0);
                            pstmt.executeUpdate();
                            isFirst = false;
//                            System.out.println("已載入商品 " + productId + " 的圖片：" + f.getName());
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("忽略非數字資料夾：" + folder.getName());
                }
            }
            System.out.println("商品圖片載入完成！");
        }
    }
}