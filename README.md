## Features

SwiftLocaleValidator will validate the localized strings in the following ways:
 - Looks for strings that are defined multiple times in the Localizable.strings
 - Looks for strings that are requested in the code using NSLocalizedString, but aren't found in the Localizable.strings
 - Looks for strings that are defined in the Localizable.strings, but aren't requested in the code using NSLocalizedString

## How to use SwiftLocaleValidator

To use SwiftLocaleValidator, first grab the .jar file available [here](https://github.com/Ferdzz/SwiftLocaleValidator/blob/master/SwiftLocaleValidator.jar) and use the following CLI command to validate your strings. It's as simple as that.

```
java -jar SwiftLocaleValidator.jar -directory "path/to the project/directory" -locale "path/to/the/Localizable.strings"
```

Note that the lack of support of multiple locale files is intentional. All locale files should be validated one by one to ensure the best quality control possible.

If your project uses custom formats instead of `NSLocalizedString`, you can specify this using the `-substring` argument. For example, if your
project uses a pattern such as `"Localizable.Key".localized()`, you should specify the following:

```
java -jar SwiftLocaleValidator.jar -directory "path/to the project/directory" -locale "path/to/the/Localizable.strings" -substring "\"" "\".localized("
```

### Sample output:
```
Parsing the project...

 --- Starting parsing locale file --- 
 --- Done parsing locale file --- 

 --- Parsing code for Localized String references --- 
 --- Done parsing code for Localized String references --- 

 --- Comparing code and locale file --- 
'ErrorMessage.NoWifi.Title' was defined in the locale but not used in code! 
'ErrorMessage.NoWifi.Message' was defined in the locale but not used in code! 
------
'ErrorMessage.Unreachable.Title' was used in the code but never defined in the locale! 
'ErrorMessage.Unreachable.Message' was used in the code but never defined in the locale! 
 --- Done comparing code and locale file, tasks finished --- 

Done parsing the project!
```