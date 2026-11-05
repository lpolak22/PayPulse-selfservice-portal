package com.example.paypulse.dto.dashboard;

import java.io.Serializable;
import java.util.List;

public record TrendResponse(TrendUnit unit, List<TrendPointResponse> points) implements Serializable {
}
