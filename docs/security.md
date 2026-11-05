# FUGEN Security & Compliance Specification

1. **Secret Isolation**: Mobile clients never contain LiveKit API secrets, database credentials, or JWT signing keys.
2. **Authentication**: JWT access tokens with 7-day expiration and automatic refresh rotation.
3. **Financial Protection**: Server-authoritative wallet ledger with zero client-side balance modification.
4. **Rate Limiting**: API gateway throttling for high-frequency endpoints (comments, likes, gift sending, guest requests).
5. **Content Moderation**: Pre-publish safety checks, community guideline compliance, and admin reporting tools.
