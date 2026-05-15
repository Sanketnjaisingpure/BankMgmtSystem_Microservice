package com.bank.config;

public class KafkaConstants {

    // ── Account Service ──

    public static final String ACCOUNT_CREATION_TOPIC = "account-creation-topic";

    public static final String ACCOUNT_CREATION_GROUP = "account-creation-group";

    public static final String TRANSACTION_NOTIFICATION_TOPIC = "transaction-notification-topic";

    public static final String TRANSACTION_NOTIFICATION_GROUP = "transaction-notification-group";

    public static final String TRANSACTION_TOPIC = "transaction-topic";

    public static final String TRANSACTION_GROUP = "transaction-group";

    public static final String TRANSACTION_PAYMENT_TOPIC = "transaction-payment-topic";

    public static final String TRANSACTION_PAYMENT_GROUP = "transaction-payment-group";


    // ── Loan Service ──

    public static final String LOAN_APPLICATION_TOPIC = "loan-application-topic";

    public static final String LOAN_APPLICATION_GROUP = "loan-application-group";

    public static final String LOAN_STATUS_TOPIC = "loan-status-topic";

    public static final String LOAN_STATUS_GROUP = "loan-status-group";

    public static final String LOAN_DISBURSEMENT_TOPIC = "loan-disbursement-topic";

    public static final String LOAN_DISBURSEMENT_GROUP = "loan-disbursement-group";

    // ── Credit Card Service ──

    public static final String CREDIT_CARD_APPLICATION_TOPIC = "credit-card-application-topic";

    public static final String CREDIT_CARD_APPLICATION_GROUP = "credit-card-application-group";

    public static final String CREDIT_CARD_STATUS_TOPIC = "credit-card-status-topic";

    public static final String CREDIT_CARD_STATUS_GROUP = "credit-card-status-group";

    public static final String CREDIT_CARD_TRANSACTION_TOPIC = "credit-card-transaction-topic";

    public static final String CREDIT_CARD_TRANSACTION_GROUP = "credit-card-transaction-group";

    // ── Bank Service ──

    public static final String BANK_REGISTRATION_TOPIC = "bank-registration-topic";

    public static final String BANK_REGISTRATION_GROUP = "bank-registration-group";

}
