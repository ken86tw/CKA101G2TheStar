package com.thestar.content.repository;

import com.thestar.content.entity.ArticleVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<ArticleVO, Integer> {

    List<ArticleVO> findFirst5ByStatusOrderByCreateAtDesc(Byte status);

    Page<ArticleVO> findByStatusOrderByCreateAtDesc(Byte status, Pageable pageable);

    Optional<ArticleVO> findByArticleIdAndStatus(Integer articleId, Byte status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ArticleVO a set a.viewCount = coalesce(a.viewCount, 0) + 1 where a.articleId = :id and a.status = 1")
    int incrementViewCount(@Param("id") Integer id);
}
