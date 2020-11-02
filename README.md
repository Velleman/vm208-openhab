
# VM208 Binding

This binding allows you to have communicate with the VM208 ecosystem on I2C bus.
It was tested with Raspberry Pi 4, but probably should work with other devices supported by [Pi4J](https://pi4j.com/) library.

On Raspberry Pi the user on which openHAB is running (default user name is "openhab") needs to be added to groups "i2c" and "gpio".

## Dependencies

Make sure that the [wiringPi](http://wiringpi.com/) library has been installed and that the `gpio` command line tool is available to openHAB.
The shared library `libwiringPi.so` is required by the [Pi4J](https://pi4j.com/) Java library to access the GPIO ports.
Without satisfying this dependency you will see strange `NoClassDefFoundError: Could not initialize class ...` errors in the openHAB logs.

## Supported Things

This binding supports one thing type:

vm208int - which is a vm208 interface module connected to an I2C bus on a specified HEX address and bus number
vm208ex - which is a vm208 relay module connected to an I2C bus on one of the 4 sockets of a vm208int

## Thing Configuration

### Required configuration for vm208int thing:

| Parameter  | Description                                                                                                                       | Default value |
|------------|-----------------------------------------------------------------------------------------------------------------------------------|---------------|
| address    | I2C bus address. On Raspberry Pi it can be checked as a result of command: "i2cdetect -y 1". Value should be set in HEX.          | "70"          |
| busNumber | a bus number to which vm208 ecosystem is connected. On RPI2, RPI3 and RPI4 it will be "1", on RPI1 it will be "0".                | "1"          |
| interruptPin | an available interrupt pin for status updates            |           |

### Required configuration for vm208ex thing:

| Parameter  | Description                                                                                                                       | Default value |
|------------|-----------------------------------------------------------------------------------------------------------------------------------|---------------|
| socket | the socket number  (1, 2, 3 or 4)     |           |
| ledReflectsRelayStatus | if true, the status of the led reflects the relay status. |  |


#### Channels

vm208ex supports 8 relays with additional functionality:

| Channel Type | Item Type | Description | R/W
|--------|----------------------------------------------------------------|-------------------------------------------|--|
| relay | SWITCH | defines if the relay is turned ON or OFF | R/W
| led   | SWITCH | defines if the led is turned ON or OFF | R/W
| button | SWITCH | defines if the button is pressed (ON) or released (OFF) | R
