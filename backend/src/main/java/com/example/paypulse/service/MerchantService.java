package com.example.paypulse.service;

import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.merchant.MerchantResponse;

public interface MerchantService {

    PageResponse<MerchantResponse> getMerchants(PageQuery pageQuery);
}
