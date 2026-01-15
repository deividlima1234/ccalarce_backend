package com.ccalarce.siglof.dto;

import com.ccalarce.siglof.model.enums.LiquidationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLiquidationRequest {
    /**
     * New status: APPROVED, REJECTED, or OBSERVED
     */
    private LiquidationStatus status;

    /**
     * Optional message from Admin to Driver
     * Required for REJECTED and OBSERVED statuses
     */
    private String note;
}
