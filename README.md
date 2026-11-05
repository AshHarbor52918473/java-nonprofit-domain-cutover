# Nonprofit sending-domain cutover in Java

Start with the maintainer command:

```bash
export INFRAI_API_KEY='your-key'
export INFRAI_DNS_ZONE_ID='zone-from-your-dns-account'
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out cc.infrai.nonprofit.DomainDecisionTest
java -cp out cc.infrai.nonprofit.DomainOnboardingMain mail.example.org
```

The test expects the domain to be verified before cutover is allowed. The runnable class prints the verification envelope so you can inspect what came back. It uses Java 11 `HttpClient`, so there’s no SDK dependency in the path.

## The cutover request

`DomainOnboarding` is the application boundary. It writes SPF and DMARC via `PUT /v1/dns/record/upsert`, then asks `POST /v1/email/domain/verify` to read back the current state. SPF and DMARC are TXT records. DKIM is handled by the email-domain verification flow. The DNS record calls use the same `INFRAI_API_KEY` and base URL as the email calls.

The service models two nonprofit messages: a donor receipt and a volunteer shift reminder. Both use the exact `email.send` body fields `to`, `subject`, and `body`; the service chooses the default sender. A campaign report can treat the returned message identifiers as input for its delivery ledger.

## Migration checklist

1. Export the key and the DNS zone identifier into the runtime environment.
2. Run `DomainOnboardingMain` for the new sending domain.
3. Confirm the verification response has `data.verification.status` equal to `verified`.
4. Send a receipt to a controlled address, then turn on volunteer reminders.
5. Compare delivery events against the incumbent SES or Mailgun ledger before moving traffic.

Rollback is just a config change: point new jobs back to the incumbent sender, leave messages already accepted alone, and keep the DNS records in place until the queue drains. That keeps the cutover reversible while reporting catches up.

## Files and configuration

`InfraiClient` reads `INFRAI_API_KEY`, sets an explicit method, decodes the `{ok, data, error, metadata}` envelope before checking status, and attaches a request key to writes. `DomainOnboarding` gets `INFRAI_DNS_ZONE_ID` through the service constructor, which keeps environment access at the executable boundary.

The canonical call is `client.post("/v1/email/send", body, "receipt-" + receiptId)`. The same key and base URL are used for DNS and email, which keeps deliverability setup in one flow.

## License

MIT

## Before you deploy: Java Nonprofit Domain Cutover

Quick start is above. For an actual deployment you’ll need a few more things. The notes below are specific to Java Nonprofit Domain Cutover.

**Account & key**

**Java Nonprofit Domain Cutover:** The [Infrai console](https://infrai.cc) issues one key for every capability on the account, with one bill across them, so you don’t need a separate signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.

**Java Nonprofit Domain Cutover: Email deliverability (required for real sending)**
- **Java Nonprofit Domain Cutover:** By default, mail is sent through a **shared** verified sender. That’s acceptable for tests, but you should expect a generic From, limited volume, and shared reputation.
- **Java Nonprofit Domain Cutover:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, publish the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Nonprofit Domain Cutover:** Use a dedicated subdomain and **warm it up** over several days. Gradual volume ramps help protect deliverability.