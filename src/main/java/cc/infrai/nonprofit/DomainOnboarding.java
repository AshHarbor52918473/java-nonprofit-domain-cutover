package cc.infrai.nonprofit;

import java.io.IOException;

/** Onboards a sending domain, then models one receipt and one volunteer reminder. */
public final class DomainOnboarding {
    private final InfraiClient client;
    private final String zoneId;

    public DomainOnboarding(InfraiClient client, String zoneId) {
        if (zoneId == null || zoneId.isBlank()) throw new IllegalArgumentException("INFRAI_DNS_ZONE_ID is required");
        this.client = client;
        this.zoneId = zoneId;
    }

    public String provisionRecords(String domain) throws IOException, InterruptedException {
        String records = "{\"zone_id\":\"" + domainValue(zoneId) + "\",\"record_type\":\"TXT\",\"name\":\"" +
                domainValue(domain) + "\",\"content\":\"v=spf1 include:send.example ~all\",\"ttl\":300}";
        client.put("/v1/dns/record/upsert", records, "dns-spf-" + domain);
        String dmarc = "{\"zone_id\":\"" + domainValue(zoneId) + "\",\"record_type\":\"TXT\",\"name\":\"_dmarc." +
                domainValue(domain) + "\",\"content\":\"v=DMARC1; p=quarantine\",\"ttl\":300}";
        return client.put("/v1/dns/record/upsert", dmarc, "dns-dmarc-" + domain);
    }

    public String verifyDomain(String domain) throws IOException, InterruptedException {
        return client.post("/v1/email/domain/verify", "{\"domain\":\"" + domainValue(domain) + "\"}", "verify-" + domain);
    }

    public String sendReceipt(String to, String amount, String receiptId) throws IOException, InterruptedException {
        String body = "{\"to\":\"" + domainValue(to) + "\",\"subject\":\"Donation receipt " + domainValue(receiptId) +
                "\",\"body\":\"Thank you for your donation of " + domainValue(amount) + "\"}";
        return client.post("/v1/email/send", body, "receipt-" + receiptId);
    }

    public String sendVolunteerReminder(String to, String date, String reminderId) throws IOException, InterruptedException {
        String body = "{\"to\":\"" + domainValue(to) + "\",\"subject\":\"Volunteer shift reminder\",\"body\":\"Your shift is on " +
                domainValue(date) + "\"}";
        return client.post("/v1/email/send", body, "volunteer-" + reminderId);
    }

    private static String domainValue(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
