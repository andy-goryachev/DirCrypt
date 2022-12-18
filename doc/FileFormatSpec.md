# File Format Specification

Version 1.


## Encrypted File Structure

|Length (bytes).|Encrypted|Type|Field|Comment|
|---|---|---|---|---|
|24|N|byte[]|random|Random bits for IV/Salt|
|8|Y|long|signature|File Signature 0x1DEA_2022_1207_2130L|
|4|Y|int|headerSize|Directory header size in bytes|
|headerSize|Y|[Header](https://github.com/andy-goryachev/DirCrypt/blob/main/src/goryachev/dircrypt/Header.java)|header|Directory header|
|--|Y|byte[]|files|Files in the order of appearance in the header.|


## Key

The key is generated using **scrypt** key derivation function, see [KeyMaterial.generate()](https://github.com/andy-goryachev/DirCrypt/blob/main/src/goryachev/dircrypt/KeyMaterial.java#L31).

The salt value is provided by a **Blake2b** 192 bit digest of the initial randomness.


## Encryption Algorithm

The rest of the file (after initial unencrypted random bits) is encrypted with
**XSalsa20** cipher, taking the initial random bits as the IV, see [XSalsaRandomAccessFile](https://github.com/andy-goryachev/DirCrypt/blob/main/src/goryachev/memsafecrypto/salsa/XSalsaRandomAccessFile.java).