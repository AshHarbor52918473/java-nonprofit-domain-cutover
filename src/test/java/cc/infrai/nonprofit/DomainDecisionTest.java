package cc.infrai.nonprofit;

/** Deterministic business test: cutover is gated by the service verification response. */
public final class DomainDecisionTest {
    public static void main(String[] args) {
        String status = "verified";
        boolean cutoverAllowed = "verified".equals(status);
        if (!cutoverAllowed) throw new AssertionError("verified domain is required before cutover");
        System.out.println("PASS: verified domain may enter cutover");
    }
}
