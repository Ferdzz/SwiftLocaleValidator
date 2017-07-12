## Features

SwiftLocaleValidator will validate the localized strings in the following ways:
 - Looks for strings that are defined multiple times in the Localizable.strings
 - Looks for strings that are requested in the code using NSLocalizedString, but aren't found in the Localizable.strings
 - Looks for strings that are defined in the Localizable.strings, but aren't requested in the code using NSLocalizedString

## How to use SwiftLocaleValidator

To use SwiftLocaleValidator, first grab the .jar file available [here](https://github.com/Ferdzz/SwiftLocaleValidator/blob/master/SwiftLocaleValidator.jar) and use the following CLI command to validate your strings. It's as simple as that.

```
java -jar SwiftLocaleValidator.jar -directory "path/to the project/directory" -locale "path/to/the/Localizable.strings
```

Note that the lack of support of multiple locale files is intentional. All locale files should be validated one by one to ensure the best quality control possible.
