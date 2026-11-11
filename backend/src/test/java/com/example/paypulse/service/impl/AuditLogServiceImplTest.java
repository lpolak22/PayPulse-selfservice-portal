package com.example.paypulse.service.impl;

import com.example.paypulse.model.AuditAction;
import com.example.paypulse.model.AuditLog;
import com.example.paypulse.repository.AuditLogRepository;
import com.example.paypulse.security.CurrentUser;
import com.example.paypulse.security.CurrentUserProvider;
import com.example.paypulse.web.ClientIpAddressResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import java.net.InetAddress;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditLogServiceImplTest {

    private final AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final ClientIpAddressResolver ipAddressResolver = mock(ClientIpAddressResolver.class);
    private final AuditLogServiceImpl service = new AuditLogServiceImpl(
            auditLogRepository, currentUserProvider, ipAddressResolver, JsonMapper.builder().build());

    @BeforeEach
    void setUp() {
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(1L, 10L));
    }

    @Test
    void recordsTheActorActionEntityAndValuesAsJson() throws Exception {
        when(ipAddressResolver.resolve()).thenReturn(Optional.of(InetAddress.getByName("10.0.0.5")));

        service.record(AuditAction.USER_UPDATED, 7L, Map.of("role", "USER"), Map.of("role", "ADMIN"));

        AuditLog entry = savedEntry();
        assertThat(entry.getActorUserId()).isEqualTo(1L);
        assertThat(entry.getAction()).isEqualTo(AuditAction.USER_UPDATED);
        assertThat(entry.getEntityType()).isEqualTo("USER");
        assertThat(entry.getEntityId()).isEqualTo(7L);
        assertThat(entry.getOldValue()).isEqualTo("{\"role\":\"USER\"}");
        assertThat(entry.getNewValue()).isEqualTo("{\"role\":\"ADMIN\"}");
        assertThat(entry.getIpAddress().getHostAddress()).isEqualTo("10.0.0.5");
    }

    @Test
    void missingValuesAndIpAddressAreStoredAsNull() {
        when(ipAddressResolver.resolve()).thenReturn(Optional.empty());

        service.record(AuditAction.USER_DELETED, 7L, Map.of("id", 7), null);

        AuditLog entry = savedEntry();
        assertThat(entry.getNewValue()).isNull();
        assertThat(entry.getIpAddress()).isNull();
    }

    private AuditLog savedEntry() {
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        return captor.getValue();
    }
}
