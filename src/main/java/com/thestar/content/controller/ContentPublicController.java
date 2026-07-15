package com.thestar.content.controller;

import com.thestar.content.dto.ArticlePublicDTO;
import com.thestar.content.dto.ReviewPublicDTO;
import com.thestar.content.entity.ReviewVO;
import com.thestar.content.service.ContentAdminService;
import com.thestar.member.entity.MemberVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/thestar/content")
public class ContentPublicController {

    private final ContentAdminService contentAdminService;

    public ContentPublicController(ContentAdminService contentAdminService) {
        this.contentAdminService = contentAdminService;
    }

    @GetMapping("/news")
    public List<ArticlePublicDTO> latestNews() {
        return contentAdminService.findLatestNews().stream()
                .map(ArticlePublicDTO::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/articles")
    public Page<ArticlePublicDTO> articles(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "6") int size) {
        return contentAdminService.findPublishedArticles(PageRequest.of(page, size))
                .map(ArticlePublicDTO::from);
    }

    @GetMapping("/articles/{id}")
    public ArticlePublicDTO articleDetail(@PathVariable Integer id) {
        return ArticlePublicDTO.fromDetail(contentAdminService.findPublishedArticleAndIncreaseViews(id));
    }

    @GetMapping("/articles/{id}/cover")
    public ResponseEntity<byte[]> articleCover(@PathVariable Integer id) {
        byte[] image = contentAdminService.findPublishedArticle(id).getCoverImage();
        if (image == null || image.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(detectImageType(image))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(image);
    }

    @GetMapping("/articles/{id}/reviews")
    public List<ReviewPublicDTO> reviews(@PathVariable Integer id) {
        return contentAdminService.findReviewsForArticle(id).stream()
                .map(ReviewPublicDTO::from)
                .collect(Collectors.toList());
    }

    @PostMapping("/articles/{id}/reviews")
    public ResponseEntity<ReviewPublicDTO> addReview(@PathVariable Integer id,
                                                      @RequestBody Map<String, String> body,
                                                      HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("loginMember");
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ReviewVO review = contentAdminService.createReview(id, member.getMemberId(), body.get("content"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewPublicDTO.from(review));
    }

    private MediaType detectImageType(byte[] image) {
        if (image.length >= 3 && (image[0] & 0xff) == 0xff && (image[1] & 0xff) == 0xd8) {
            return MediaType.IMAGE_JPEG;
        }
        if (image.length >= 8 && (image[0] & 0xff) == 0x89 && image[1] == 0x50
                && image[2] == 0x4e && image[3] == 0x47) {
            return MediaType.IMAGE_PNG;
        }
        if (image.length >= 6 && image[0] == 0x47 && image[1] == 0x49 && image[2] == 0x46) {
            return MediaType.IMAGE_GIF;
        }
        if (image.length >= 12 && image[0] == 0x52 && image[1] == 0x49 && image[2] == 0x46
                && image[3] == 0x46 && image[8] == 0x57 && image[9] == 0x45
                && image[10] == 0x42 && image[11] == 0x50) {
            return MediaType.valueOf("image/webp");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
