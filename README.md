# QuantumHack (Fork of Wurst v7)

## Downloads (for users)

[Download QuantumHack](https://foldesandras.github.io/quantumhack)

## Setup (for developers)

(This guide assumes you're using Windows with [Eclipse](https://www.eclipse.org/downloads/) and [Java Development Kit 17](https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot) already installed.)

1. Run the following command in PowerShell:

    ```
    ./gradlew.bat genSources eclipse --no-daemon
    ```

2. In Eclipse, go to `Import...` > `Existing Projects into Workspace`, and select this project.

## Contributing

Pull requests are welcome! However, please make sure to review the [contributing guidelines](CONTRIBUTING.md) before submitting your changes.

## Translations

To submit translations, the preferred method is to create a Pull Request on GitHub.

To enable in-game translations, go to `QuantumHack Options > Translations > ON`.

Please note: Names of features (such as hacks, commands, etc.) should remain in English to ensure consistency across different language settings. This also allows for easier communication between users of different languages.

Translation files can be found in [this folder](https://github.com/foldesandras/QuantumHack/tree/master/src/main/resources/assets/quantumhack/translations) if you need them.

## License

This code is licensed under the GNU General Public License v3. **Please note that this code can only be used in open-source clients that you release under the same license.** Using this code in closed-source or proprietary clients is strictly prohibited.

---

### Important note for users:
While tools like QuantumHack (or other modded clients) may provide additional functionality, **please ensure you only use them in single-player or on servers where cheats and mods are allowed**. On servers where cheating is prohibited, using such clients may violate the rules and result in bans or penalties. Always respect the community rules and the fair play environment of multiplayer servers.
