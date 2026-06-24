package com.credit.loan.model;

public enum LoanStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    DECLINED;

    public static LoanStatus fromString(String statusStr) {
        if (statusStr == null) {
            return null;
        }
        String normalized = statusStr.trim().toUpperCase();
        if (normalized.equals("APPROVE") || normalized.equals("APPROVED")) {
            return APPROVED;
        }
        if (normalized.equals("DECLINE") || normalized.equals("DECLINED")) {
            return DECLINED;
        }
        if (normalized.equals("UNDER_REVIEW")) {
            return UNDER_REVIEW;
        }
        if (normalized.equals("SUBMITTED")) {
            return SUBMITTED;
        }
        return LoanStatus.valueOf(normalized);
    }
}
