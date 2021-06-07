# DiaBEATit Android App
This repository holds the code for the DiaBEATit App supporting our diabetes research.
It is based on the code from a "Bachelor Praktikum" I supervised in 2019 and the [AndroidAPS](https://github.com/MilosKozak/AndroidAPS) project.
The goal of the project was to collect and export data relevant for diabetes patients in a convenient way to support our diabetes research efforts.

Check the Wiki page for documentation.
Check our [website](https://diabeatit.de) for more information about the project.

## Make it compile
The DiaBEATit App uses the "chaquopy" library to run python algorithms within the app. Therefore you'll need to install python3 on your computer and tell chaquopy where to find it. Add the following to your `local.properties`:
```
chaquopy.license=YOUR-LICENSE-KEY
python.path=C:\\PATH\\TO\\python.exe
```

## Disclaimer And Warning
* All information, thought, and code described here is intended for informational and educational purposes only. DiaBEATit currently makes no attempt at HIPAA privacy compliance. Use DiaBEATit at your own risk, and do not use the information or code to make medical decisions.
* Use of code from github.com is without warranty or formal support of any kind. Please review this repository’s LICENSE for details.
* All product and company names, trademarks, servicemarks, registered trademarks, and registered servicemarks are the property of their respective holders. Their use is for information purposes and does not imply any affiliation with or endorsement by them.
