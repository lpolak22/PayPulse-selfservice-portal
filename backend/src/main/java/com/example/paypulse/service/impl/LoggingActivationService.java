package com.example.paypulse.service.impl;

import com.example.paypulse.model.AppUser;
import com.example.paypulse.service.ActivationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingActivationService implements ActivationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingActivationService.class);

    @Override
    public void startActivation(AppUser user) {
        log.info("Activation email for user {} is due: it is sent once the activation flow (PULSE-43) is merged",
                user.getIdUser());
    }
}
