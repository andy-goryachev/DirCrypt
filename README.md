# DirCrypt
Command Line Utility for Encrypting Folders.

Uses [scrypt](https://en.wikipedia.org/wiki/Scrypt)
for password-based key derivation function and 
[XSalsa20](https://en.wikipedia.org/wiki/Salsa20#XChaCha) cipher.

**Download:** [DirCrypt.jar](https://github.com/andy-goryachev/DirCrypt/raw/main/dist/DirCrypt.jar)

## Encrypt

```
  java -jar DirCrypt.jar --enc --out FILE [options] DIR1 DIR2 ...
```

## Decrypt

```
  java -jar DirCrypt.jar --dec --in FILE [options] --dest OUTDIR
```

## List Archive Contents

```
  java -jar DirCrypt.jar --list --in FILE --list
```

## Options

| Option | Argument | Description | Default Value | Required For |
|---|---|---|---|---|
|--dest|Directory|Specifies the destination directory| |Decryption|
|--force| |Overwrites output file(s)| |Decryption|
|--help| |Prints usage| | |
|--in|File|Specifies input archive file name| |Encryption,Listing|
|--list| |Lists file contents| |Listing|
|--out|File|Specifies output archive file| |Encryption|
|--pass|Text|Passphrase| | |
|--scryptN|Integer|Scrypt N parameter|32768| |
|--scryptP|Integer|Scrypt P parameter|32| |
|--scryptR|Integer|Scrypt R parameter|16| |
|--verbose| |Logs diagnostic messages to stdout| | |
|--version| |Prints version string| | |


## Security

Please refer to the [File Format Specification](doc/FileFormatSpec.md).


## WARNING

The code has not been formally reviewed.  Do not use in production.

