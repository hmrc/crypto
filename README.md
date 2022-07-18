# Crypto

[![Build Status](https://travis-ci.org/hmrc/crypto.svg)](https://travis-ci.org/hmrc/crypto) [ ![Download](https://api.bintray.com/packages/hmrc/releases/crypto/images/download.svg) ](https://bintray.com/hmrc/releases/crypto/_latestVersion)

A micro-library for all Crypto related infrastructure.

In the example below 'cookie.encryption' is the baseConfigKey that is specified
when the crypto utility is created.
The 'previousKeys' element is optional.

```conf
# Base 64 encoded MD5 hash of application.secret

cookie.encryption {
  key="gvBoGdgzqG1AarzF1LY0zQ=="
  previousKeys=["AwMDAwMDAwMDAwMDAwMDAw==","BAQEBAQEBAQEBAQEBAQEBA=="]
}
```

## Installing

Add the following to your SBT build:

```scala
resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")

libraryDependencies += "uk.gov.hmrc" %% "crypto" % "[INSERT-VERSION]"
```

## Changes

### Version 7.0.0

- The `secure` library has been rolled into `crypto`. The package has changed from `hmrc.gov.uk.secure` to `hmrc.gov.uk.crypto.secure`.
- `SecureGCMCipher` has been added. It is different from `GCMCrypto` in that it supports associated data to be provided on each encrypt/decrypt.



## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
