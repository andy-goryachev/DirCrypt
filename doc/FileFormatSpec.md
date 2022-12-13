# File Format Specification

Version 1.

|Length (bytes).|Encrypted|Type|Field|Comment|
|---|---|---|---|---|
|24|N|byte[]|RAND|Initial randomness|
|8|Y|long|SIGNATURE|File Signature 0x1DEA_2022_1207_2130L|
|4|Y|int|headerSize|Directory header size in bytes|
