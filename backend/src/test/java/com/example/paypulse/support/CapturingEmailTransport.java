package com.example.paypulse.support;

import com.example.paypulse.dto.email.OutgoingEmail;
import com.example.paypulse.service.EmailTransport;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CapturingEmailTransport implements EmailTransport {

    private final LinkedBlockingQueue<OutgoingEmail> delivered = new LinkedBlockingQueue<>();

    @Override
    public void deliver(OutgoingEmail email) {
        delivered.add(email);
    }

    public OutgoingEmail awaitNext(Duration timeout) throws InterruptedException {
        return delivered.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    public List<OutgoingEmail> delivered() {
        return List.copyOf(delivered);
    }

    public void clear() {
        delivered.clear();
    }
}
