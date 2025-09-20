package com.locallocket.backend.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundResponse {
    private String refundId;
    private BigDecimal amount;
    private String status;
    private String reason;
    private LocalDateTime processedAt;

    // Default constructor
    public RefundResponse() {}

    // Constructor
    public RefundResponse(String refundId, BigDecimal amount, String status, String reason) {
        this.refundId = refundId;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
        this.processedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
