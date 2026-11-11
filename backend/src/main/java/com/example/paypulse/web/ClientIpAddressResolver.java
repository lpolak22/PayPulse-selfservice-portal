package com.example.paypulse.web;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

@Component
public class ClientIpAddressResolver {

    public Optional<InetAddress> resolve() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return Optional.empty();
        }
        return parse(attributes.getRequest().getRemoteAddr());
    }

    private static Optional<InetAddress> parse(String address) {
        try {
            return Optional.of(InetAddress.getByName(address));
        } catch (UnknownHostException exception) {
            return Optional.empty();
        }
    }
}
