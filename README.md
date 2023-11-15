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

Represented by the model `Encrypter` and `Decrypter`. Or `AdEncrypter` `AdDecrypter` for the associated data variant.

The different variants are provided by `SymmetricCryptoFactory`.

The supported types are:

- `AesCrypto`

  An implementation of "AES" Cipher.

  It represents the encypted data as `Crypted`, which contains a single base64 encoded String.

  Note, it is recommended to use `AesGCMCrypto` instead which uses a nonce to prevent repeatable encryptions.

  To create, either call `SymmetricCryptoFactory.aesCrypto` with the secret key, or `SymmetricCryptoFactory.aesCryptoFromConfig` to look up the keys from config. `SymmetricCryptoFactory.aesCryptoFromConfig` additionally supports decrypting with any available previous keys, to support key rotation.

- `AesGCMCrypto`

  Similar to AesCrypto, but uses the GCM algorithm. This includes the use of a nonce, to prevent repeatable encryptions. Note, the associated data is always set to an empty array. Use `AesGcmAdCrypto` if setting the associated data is required.

  It represents the encypted data as `Crypted`, which contains a single base64 encoded String.

  To create, either call `SymmetricCryptoFactory.aesGcmCrypto` with the secret key, or `SymmetricCryptoFactory.aesGcmCryptoFromConfig` to look up the keys from config.  `SymmetricCryptoFactory.aesGcmCryptoFromConfig` additionally supports decrypting with any available previous keys, to support key rotation.

- `AesGcmAdCrypto`

  It is similar to `AesGCMCrypto`, but it additionally takes some associated data when encrypting and decrypting. This can be used to prevent copying encrypted data to another context.

  It is a replacement to `SecreGCMCipher` that was previously included in many clients; and to simplify migration, it represents the encrypted data with `EncryptedValue` rather than `Crypted`.

  Note, if you are migrating from `SecureGCMCipher`, you will provide the key (and any previous keys) to the construction of `AesGcmAdCrypto` and not to each call to `encrypt`/`decrypt`. You will also need to import `CryptoFormats.encryptedValueFormat` from `crypto-json`.

  To create, either call `SymmetricCryptoFactory.aesGcmAdCrypto` with the secret key, or `SymmetricCryptoFactory.aesGcmAdCryptoFromConfig` to look up the keys from config. `SymmetricCryptoFactory.aesGcmAdCryptoFromConfig` additionally supports decrypting with any available previous keys, to support key rotation.

See [java docs](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html) for more details.

### Oneway hashers/verifiers

Represented by the model `Hasher` and `Verifier`.

The supported variants are provided by `OnewayCryptoFactory`.

### Sensitive

This model identifies data which should be encrypted. It can be used in conjunction with [Crypto Json](#crypto-json) to encrypt in JSON for storing in database or sending over the wire. It also overrides `toString` to suppress logging.

It is recommended to use `Sensitive` rather than `Protected` as provided by `json-encyption` since the parameterised type is not erased, which can be useful with looking up a mongo codec in runtime for example.

### Crypto Json

Provides Play json formats which encrypt the `Sensitive` type. See [Sensitive](#sensitive).

This replaces the `json-encryption` library.

```scala
resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")

libraryDependencies += "uk.gov.hmrc" %% "crypto-json-play-xx" % "[INSERT-VERSION]"
```

Where `play-xx` is your version of Play (e.g. `play-28`).

#### Migrating from json-encryption library

You can use a provided `Sensitive` implementation and encrypter/decrypter if available. E.g. for `String`

```scala
val value: String = ...
val encryptor = new JsonEncryptor[String]()
val encryptedValue: JsValue = encryptor.writes(Protected[String](value))
```

becomes

```scala
val value: String = ...
val encrypter = JsonEncryption.stringEncrypter
val encryptedValue: JsValue = encrypter.writes(SensitiveString(value))
```

and

```scala
val encryptedValue: JsValue = ...
val decryptor = new JsonDecryptor[String]()
val optValue: Option[String] = decryptor.reads(encryptedValue).asOpt.map(_.decryptedValue)
```

becomes

```scala
val encryptedValue: JsValue = ...
val decrypter = JsonEncryption.stringDecrypter
val optValue: Option[String] = decrypter.reads(encryptedValue).asOpt.map(_.decryptedValue)
```

If there isn't a `Sensitive` implementation provided for your required type, you can create one.

For a generic type, the type variable can be added on your `Sensitive` implementation (but in this example, `T` will be erased - but this is the same as the deprecated `Protected`). E.g. using

```scala
case class SensitiveT[T](override val decryptedValue: T) extends Sensitive[T]
```

```scala
val value: T = ...
val encryptor = new JsonEncryptor[T]()
val encryptedValue: JsValue = encryptor.writes(Protected[T](value))
```

becomes

```scala
val value: T = ...
val encrypter = JsonEncryption.sensitiveEncrypter[T, SensitiveT[T]]
val encryptedValue: JsValue = encrypter.writes(SensitiveT(value))
```

and

```scala
val encryptedValue: JsValue = ...
val decryptor = new JsonDecryptor[T]()
val optValue: Option[T] = decryptor.reads(encryptedValue).asOpt.map(_.decryptedValue)
```

becomes

```scala
val encryptedValue: JsValue = ...
val decrypter = JsonEncryption.sensitiveDecrypter[T, SensitiveT[T]](SensitiveT.apply)
val optValue: Option[T] = decrypter.reads(encryptedValue).asOpt.map(_.decryptedValue)
```

## Changes

### Version 7.6.0

Adds support for Play 2.9 and Play 3.0

### Version 7.0.0

- The `secure` library has been rolled into `crypto`. The package has changed from `hmrc.gov.uk.secure` to `hmrc.gov.uk.crypto.secure`.
- The artefact `crypto-json-play-xx` has been added to replace the `json-encryption` library. It provides `Sensitive` rather than `Provided` to avoid erasure and doesn't leak the value in `toString`.
- `AesGcmAdCrypto` has been added. It is different from `AesGCMCrypto` in that it supports associated data to be provided on each encrypt/decrypt. This should replace custom `SecureGCMCipher`.
- `SymmetricCryptoFactory` has been added to make finding/using symetric cryptos easier.
- `CompositeSymmetricCrypto` has been deprecated. To compose cryptos, clients should use `SymmetricCryptoFactory.composeCrypto`. Clients should not use the `CompositeSymmetricCrypto` abstraction, which is implementation details of the composition of previous decrypters. Instead, they should use `Encrypter with Decrypter`.



## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
