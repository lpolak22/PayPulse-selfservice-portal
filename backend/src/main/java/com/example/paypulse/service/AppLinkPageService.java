package com.example.paypulse.service;

import com.example.paypulse.web.AppLinkTarget;

public interface AppLinkPageService {

    boolean isWellFormedToken(String token);

    String render(AppLinkTarget target, String token);
}
