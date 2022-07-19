# Crypto

![](https://img.shields.io/github/v/release/hmrc/crypto)

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

## Use

### Symmetric encrypter/decrypters

There are 3 flavours:

- `AesCrypto`

  An implementation of "AES" Cipher.

  It represents the encypted data as `Crypted`, which contains a single base64 encoded String.

  To create, either call `CompositeSymmetricCrypto.aes` with the secret keys, or instantiate `CryptoWithKeysFromConfig` to look up the keys from config. These both take previous keys for decryption to support key rotation.

- `AesGCMCrypto`

  Similar to AesCrypto, but uses the GCM algorithm. This includes the use of a nonce. Note, the associated data is always set to an empty array. Use `CryptoGcmAd` if setting the associated data is required.

  It represents the encypted data as `Crypted`, which contains a single base64 encoded String.

  To create, either call `CompositeSymmetricCrypto.aesGCM` with the secret keys, or instantiate `CryptoGCMWithKeysFromConfig` to look up the keys from config.
  These both take previous keys for decryption to support key rotation.

- `CryptoGcmAd`

  It is similar to `AesGCMCrypto`, but it additionally takes some associated data when encrypting and decrypting.

  It is a replacement to `SecreGCMCipher` that was previously included in many clients; and to simplify migration, it represents the encrypted data with `EncryptedValue` rather than `Crypted`.

  Create by either instantiating `CryptoGcmAd` with the secret keys, or `CryptoGcmAdFromConfig` to look up the keys from config. These both take previous keys for decryption to support key rotation.

See [java docs](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html) for more details.


## Changes

### Version 7.0.0

- The `secure` library has been rolled into `crypto`. The package has changed from `hmrc.gov.uk.secure` to `hmrc.gov.uk.crypto.secure`.
- `CryptoGcmAd` has been added. It is different from `AesGCMCrypto` in that it supports associated data to be provided on each encrypt/decrypt.



## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
