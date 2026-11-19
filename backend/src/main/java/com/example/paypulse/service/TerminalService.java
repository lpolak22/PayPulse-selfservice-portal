package com.example.paypulse.service;

import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.terminal.TerminalResponse;
import com.example.paypulse.dto.terminal.TerminalSearchCriteria;
import com.example.paypulse.dto.terminal.TerminalSort;

public interface TerminalService {

    PageResponse<TerminalResponse> getTerminals(TerminalSearchCriteria criteria, TerminalSort sort,
                                                PageQuery pageQuery);
}
