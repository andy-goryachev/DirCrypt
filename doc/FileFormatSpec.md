# File Format Specification

Version 1.

|Length (bytes).|Encrypted|Type|Field|Comment|
|---|---|---|---|---|
|24|N|byte[]|random|Initial randomness|
|8|Y|long|signature|File Signature 0x1DEA_2022_1207_2130L|
|4|Y|int|headerSize|Directory header size in bytes|
|headerSize|Y|[Header](https://github.com/andy-goryachev/DirCrypt/blob/main/src/goryachev/dircrypt/Header.java)|header|Directory header|
|--|Y|byte[]|files|Files in the order of appearance in the header.|
