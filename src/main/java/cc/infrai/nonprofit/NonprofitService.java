package cc.infrai.nonprofit;

import java.io.IOException;

/** Spring-style service boundary: configuration enters here, domain actions stay explicit. */
public final class NonprofitService {
    private final DomainOnboarding onboarding;

    public NonprofitService(DomainOnboarding onboarding) {
        this.onboarding = onboarding;
    }

    public String onboard(String domain) throws IOException, InterruptedException {
        onboarding.provisionRecords(domain);
        return onboarding.verifyDomain(domain);
    }
}
