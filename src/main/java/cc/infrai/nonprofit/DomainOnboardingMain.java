package cc.infrai.nonprofit;

/** Runnable cutover example. */
public final class DomainOnboardingMain {
    public static void main(String[] args) throws Exception {
        String key = required("INFRAI_API_KEY");
        String zoneId = required("INFRAI_DNS_ZONE_ID");
        String domain = args.length > 0 ? args[0] : "mail.example.org";
        DomainOnboarding workflow = new DomainOnboarding(new InfraiClient(key), zoneId);
        workflow.provisionRecords(domain);
        String verification = workflow.verifyDomain(domain);
        System.out.println("domain verification response: " + verification);
        System.out.println("next: upsert SPF and DMARC, then verify again before cutover");
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value;
    }
}
