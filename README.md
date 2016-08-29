# TOTP-Authenticator

A Time based two factor authentication code generator like Google-Authenticator.

Sync your website key to firebase database.

* Calculate time based OTP code like Google-Authenticator
* Multi account support
* Scan QR code load new website configuration
* Sync data with firebase database, recover after new installation (same google account)

## Algorithm

Please refer to [this](https://en.wikipedia.org/wiki/Time-based_One-time_Password_Algorithm) for details.

Like Google-Authenticator, time-counter is 30 seconds.

## Security

Please use your own firebase or database to save secrets.

![img](art/img1.jpg)