# Controller Server

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Controller%3AController&metric=alert_status)](https://sonarcloud.io/dashboard?id=Controller%3AController)

Server for Controller app, see Android application that allows to control PC mouse and keyboard (and more). Requires client application to use - [Controller Client](https://github.com/lulewiczg/ControllerClient), or can be used with custom client that complies with server API.

<img alt="Server window" src="https://i.imgur.com/e9FNXrR.png"></a>

## Getting Started

To run this application, you need:
* Windows or Linux
* Java 10 or higher

### Installing
Build this app from IDE or directly, using Maven.
Or [download latest version](https://github.com/lulewiczg/ControllerServer/releases/latest/download/Controller.jar).

## Usage
To connect to this server as client, LoginAction should be sent with proper password. Then, actions can be sent to be processed by server.
Server disconnects client after configurable idle time.

Settings are stored in *application.properties*, file is created in jar file directory, when default settings are changed.

**Server states**:
- **Waiting** - server waits for connection
- **Connected** - client connected
- **Shutdown** - server stopped


**Available settings**:
- *com.github.lulewiczg.logging.pattern* - logging pattern
- *com.github.lulewiczg.setting.port* - server port
- *com.github.lulewiczg.setting.password* - server password
- *com.github.lulewiczg.setting.autostart* - should server start when application start
- *com.github.lulewiczg.setting.userFile* - user settings file name
- *com.github.lulewiczg.setting.logLevel* - logging level
- *com.github.lulewiczg.setting.serverTimeout* - max idle time in miliseconds


### Running application
-  **Window mode**

Just run JAR file.

```
java -jar server.jar
```

- **Console mode**

Run JAR file with *console parameter*

```
java -jar server.jar console
```

## Features
- Windowed or console mode
- Displaying current PC IPs
- Configuration in window or .properties file
- Logs displayed also in window
- Configurable log level in window (ALL in log file)
- Clearing logs

#### Supported actions
- Mouse button press & release
- Mouse scroll
- Mouse move
- Keyboard key press & release
- Text copy from client & paste in host
- Ping (to keep connection alive)
- Disconnect
- Stop server

## Known Bugs
- Alt key not working on Windows (accessibility limitations)

## TODO
- Change java serialization to JSON
- I18n
- Add action keys like volume up, etc.
- CMD?