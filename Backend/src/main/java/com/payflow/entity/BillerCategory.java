package com.payflow.entity;

import lombok.Getter;

@Getter
public enum BillerCategory {
    MOBILE_PREPAID("Mobile Prepaid"),
    MOBILE_POSTPAID("Mobile Postpaid"),
    DTH("DTH / Cable TV"),
    BROADBAND("Broadband"),
    LANDLINE("Landline"),
    ELECTRICITY("Electricity"),
    WATER("Water"),
    GAS("Gas"),
    INSURANCE("Insurance"),
    CREDIT_CARD("Credit Card"),
    LOAN_EMI("Loan EMI"),
    FASTAG("FASTag"),
    SUBSCRIPTION("Subscription"),
    OTHER("Other");

    private final String displayName;

    BillerCategory(String displayName) {
        this.displayName = displayName;
    }
}