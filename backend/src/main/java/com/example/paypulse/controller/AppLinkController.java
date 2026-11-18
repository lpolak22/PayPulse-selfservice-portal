package com.example.paypulse.controller;

import com.example.paypulse.service.AppLinkPageService;
import com.example.paypulse.web.AppLinkTarget;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Hidden
@Controller
@RequestMapping("/open")
public class AppLinkController {

    private static final MediaType HTML_UTF8 = new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8);
    private static final String CONTENT_SECURITY_POLICY = "default-src 'none'; style-src 'unsafe-inline'";

    private final AppLinkPageService pageService;

    public AppLinkController(AppLinkPageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping("/{target}")
    public ResponseEntity<String> open(@PathVariable String target,
                                       @RequestParam(required = false) String token) {
        Optional<AppLinkTarget> linkTarget = AppLinkTarget.fromPath(target);
        if (linkTarget.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        HttpStatus status = pageService.isWellFormedToken(token) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .contentType(HTML_UTF8)
                .cacheControl(CacheControl.noStore())
                .header("Referrer-Policy", "no-referrer")
                .header("Content-Security-Policy", CONTENT_SECURITY_POLICY)
                .header("X-Robots-Tag", "noindex")
                .body(pageService.render(linkTarget.get(), token));
    }
}
