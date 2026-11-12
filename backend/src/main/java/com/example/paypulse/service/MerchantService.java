package com.example.paypulse.service;

import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.merchant.MerchantResponse;
import com.example.paypulse.dto.merchant.MerchantSearchCriteria;
import com.example.paypulse.dto.merchant.MerchantSort;

public interface MerchantService {

    PageResponse<MerchantResponse> getMerchants(MerchantSearchCriteria criteria, MerchantSort sort,
                                                PageQuery pageQuery);
}
