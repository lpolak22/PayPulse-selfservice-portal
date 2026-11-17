package com.example.paypulse.service;

import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.outlet.OutletResponse;
import com.example.paypulse.dto.outlet.OutletSearchCriteria;

import java.util.List;

public interface OutletService {

    PageResponse<OutletResponse> getOutlets(Long merchantId, OutletSearchCriteria criteria, PageQuery pageQuery);

    List<String> getCities(Long merchantId);
}
