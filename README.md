# eEWA Wallet App for Android

Based on EU reference implementation https://github.com/eu-digital-identity-wallet/eudi-app-android-wallet-ui

Changes made for Phase 2 submission:
- updated look & feel of the app
- support for storing and presenting EAAs in mDoc or SD-JWT format
- if applet is available, key material for EAAs is derived from Secure Element
- parse EAA issuer metadata and use that for styling EAAs in the app
- support for vct document filter in presentation flow
- support for new PID type https://metadata-8c062a.usercontent.opencode.de/pid.json
- support for Verifier Attestation

Changes made for Phase 1 submission:
- new look & feel of the app
- removed / hid features that are not required by Funke stage 1
- support for authenticated channel
- support for either PID from Secure Element or proxy (temporary) PID used once during verifier flow
- support for SD-JWT