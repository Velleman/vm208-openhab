# VM208 Binding

This binding allows you to have communicate with the VM208 ecosystem on I2C bus.
It was tested with Raspberry Pi 2 and Raspberry Pi 3, but probably should work with other devices supported by [Pi4J](https://pi4j.com/) library.

On Raspberry Pi the user on which openHAB is running (default user name is "openhab") needs to be added to groups "i2c" and  "gpio".

## Dependencies

Make sure that the [wiringPi](http://wiringpi.com/) library has been installed and that the `gpio` command line tool is available to openHAB.
The shared library `libwiringPi.so` is required by the [Pi4J](https://pi4j.com/) Java library to access the GPIO ports.
Without satisfying this dependency you will see strange `NoClassDefFoundError: Could not initialize class ...` errors in the openHAB logs.

## Supported Things

This binding supports one thing type:

vm208int - which is a vm208 interface module connected to an I2C bus on specified HEX address and bus number
vm208ex - which is a vm208 relay module connected to an I2C bus on one of the 4 sockets of a vm208int

## Thing Configuration

* Required configuration for mcp23017 thing:

| Parameter  | Description                                                                                                                       | Default value |
|------------|-----------------------------------------------------------------------------------------------------------------------------------|---------------|
| address    | MCP23017 I2C bus address. On Raspberry Pi it can be checked as a result of command: "i2cdetect -y 1". Value should be set in HEX. | "20"          |
| bus_number | a bus number to which mcp23017 is connected. On RPI2 and RPI3 it will be "1", on RPI1 it will be "0".                             | "1"           |

## Channels

mcp23017 supports 16 channels in 2 groups:

| Group  | Channels                                                       | Additional parameters                     |
|--------|----------------------------------------------------------------|-------------------------------------------|
| input  | A0, A1, A2, A3, A4, A5, A6, A7, B0, B1, B2, B3, B4, B5, B6, B7 | pull_mode (OFF, PULL_UP), default is OFF  |
| output | A0, A1, A2, A3, A4, A5, A6, A7, B0, B1, B2, B3, B4, B5, B6, B7 | default_state (LOW, HIGH), default is LOW |

Channel determines MCP23017 PIN we want to use.

Group determines mode in which PIN shoud work.

When PIN should work as DIGITAL_INPUT, channel from group "input" should be used.

When PIN should work as DIGITAL_OUTPUT, channel from group "output" should be used.