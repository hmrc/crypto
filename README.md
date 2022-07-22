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

  To create, either call `SymmetricCryptoFactory.aesCrypto` with the secret key, or `SymmetricCryptoFactory.aesCryptoFromConfig` to look up the keys from config. `SymmetricCryptoFactory.aesCryptoFromConfig` additionally supports decrypting with any available previous keys, to support key rotation.

- `AesGCMCrypto`

  Similar to AesCrypto, but uses the GCM algorithm. This includes the use of a nonce. Note, the associated data is always set to an empty array. Use `AesGcmAdCrypto` if setting the associated data is required.

  It represents the encypted data as `Crypted`, which contains a single base64 encoded String.

  To create, either call `SymmetricCryptoFactory.aesGcmCrypto` with the secret key, or `SymmetricCryptoFactory.aesGcmCryptoFromConfig` to look up the keys from config.  `SymmetricCryptoFactory.aesGcmCryptoFromConfig` additionally supports decrypting with any available previous keys, to support key rotation.

- `AesGcmAdCrypto`

  It is similar to `AesGCMCrypto`, but it additionally takes some associated data when encrypting and decrypting.

  It is a replacement to `SecreGCMCipher` that was previously included in many clients; and to simplify migration, it represents the encrypted data with `EncryptedValue` rather than `Crypted`.

  Note, if you are migrating from `SecureGCMCipher`, you will provide the key (and any previous keys) to the construction of `AesGcmAdCrypto` and not to each call to `encrypt`/`decrypt`. You will also need to import `CryptoFormats.encryptedValueFormat` from `json-encryption`.

  To create, either call `SymmetricCryptoFactory.aesGcmAdCrypto` with the secret key, or `SymmetricCryptoFactory.aesGcmAdCryptoFromConfig` to look up the keys from config. `SymmetricCryptoFactory.aesGcmAdCryptoFromConfig` additionally supports decrypting with any available previous keys, to support key rotation.

See [java docs](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html) for more details.

### Protected and Sensitive

This model identifies data which should be encrypted. They can be used in conjunction with [Json encrypters](#json-encryption) to encrypt in JSON for storing in database or sending over the wire. They also override `toString` to suppress logging.

Where as `Protected` is a case class with parameterised type, `Sensitive` is a trait with concrete implementations, which can be useful when erasure is problematic (e.g. looking up a mongo codec in runtime).

### Json encryption

Provides Play json formats which encrypt the `Protected` and `Sensitive` types. See [more on the model](#protected-and-sensitive).

```scala
resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")

libraryDependencies += "uk.gov.hmrc" %% "json-encryption-play-xx" % "[INSERT-VERSION]"
```

Where `play-xx` is your version of Play (e.g. `play-28`).



## Changes

### Version 7.0.0

- The `secure` library has been rolled into `crypto`. The package has changed from `hmrc.gov.uk.secure` to `hmrc.gov.uk.crypto.secure`.
- The `json-encryption` library has been rolled in as a multi-module build.
- Default `toString` of `Protected` is suppressed.
- `AesGcmAdCrypto` has been added. It is different from `AesGCMCrypto` in that it supports associated data to be provided on each encrypt/decrypt.
- `SymmetricCryptoFactory` has been added to make finding/using symetric cryptos easier.
- `CompositeSymmetricCrypto` has been deprecated. To compose cryptos, clients should use `SymmetricCryptoFactory.composeCrypto`. Clients should not use the `CompositeSymmetricCrypto` abstraction, which is implementation details of the composition of previous decrypters. Instead, they should use `Encrypter with Decrypter`.



## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
