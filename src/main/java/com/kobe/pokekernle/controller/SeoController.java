package com.kobe.pokekernle.controller;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * packageName    : com.kobe.pokekernle.controller
 * fileName       : SeoController
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    : SEO 관련 컨트롤러 (Sitemap 생성)
 */
@RestController
@RequiredArgsConstructor
public class SeoController {

    private final CardRepository cardRepository;

    @Value("${app.site.url:http://localhost:8080}")
    private String siteUrl;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 메인 페이지
        addUrl(xml, siteUrl + "/", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE), "daily", "1.0");

        // 카드 목록 페이지
        addUrl(xml, siteUrl + "/cards", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE), "daily", "0.8");

        // 컬렉션 페이지
        addUrl(xml, siteUrl + "/collection", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE), "weekly", "0.7");

        // 각 카드 상세 페이지
        List<Card> cards = cardRepository.findAll();
        for (Card card : cards) {
            addUrl(xml, siteUrl + "/cards/" + card.getId(), 
                   card.getUpdatedAt() != null ? card.getUpdatedAt().format(DateTimeFormatter.ISO_DATE) : LocalDateTime.now().format(DateTimeFormatter.ISO_DATE),
                   "weekly", "0.6");
        }

        xml.append("</urlset>");
        return xml.toString();
    }

    private void addUrl(StringBuilder xml, String loc, String lastmod, String changefreq, String priority) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(loc)).append("</loc>\n");
        xml.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
        xml.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        xml.append("    <priority>").append(priority).append("</priority>\n");
        xml.append("  </url>\n");
    }

    private String escapeXml(String str) {
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
}

