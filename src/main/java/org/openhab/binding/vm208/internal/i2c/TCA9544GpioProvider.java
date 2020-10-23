package org.openhab.binding.vm208.internal.i2c;

import java.io.IOException;

import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.GpioProviderBase;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class TCA9544GpioProvider extends GpioProviderBase implements GpioProvider {

    public static final String NAME = "com.pi4j.gpio.extension.tca.TCA9544GpioProvider";
    public static final String DESCRIPTION = "TCA9544 GPIO Provider";

    public static final int DEFAULT_POLLING_TIME = 50;

    public static final int REGISTER_CONTROL = 0x00;

    private int currentStates = 0;

    private boolean i2cBusOwner = false;
    private I2CBus bus;
    private I2CDevice device;

    public TCA9544GpioProvider(int busNumber, int address) throws UnsupportedBusNumberException, IOException {
        // create I2C communications bus instance
        this(I2CFactory.getInstance(busNumber), address, DEFAULT_POLLING_TIME);

        // create I2C device instance
        device = bus.getDevice(address);

        i2cBusOwner = true;
    }

    public TCA9544GpioProvider(I2CBus bus, int address, int pollingTime) throws IOException {
        // set reference to I2C communications bus instance
        this.bus = bus;

        // create I2C device instance
        device = bus.getDevice(address);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void changeChannel(byte channelSelect) throws IOException {
        currentStates = ((currentStates & 0b11110000) | (0b100 | channelSelect));
        this.device.write((byte) currentStates);
    }

    public int readInterrupts() throws IOException {
        currentStates = this.device.read(this.device.getAddress());
        return currentStates >> 4;
    }

    public int currentChannel() throws IOException {
        currentStates = this.device.read(this.device.getAddress());
        return currentStates & 0b11;
    }

    @Override
    public void shutdown() {
        if (isShutdown()) {
            return;
        }
        super.shutdown();
        try {
            // if we are the owner of the I2C bus, then close it
            if (i2cBusOwner) {
                // close the I2C bus communication
                bus.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
