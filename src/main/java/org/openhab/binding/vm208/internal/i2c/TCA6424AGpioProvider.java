package org.openhab.binding.vm208.internal.i2c;

import java.io.IOException;

import org.mapdb.Atomic.String;

import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.GpioProviderBase;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class TCA6424AGpioProvider extends GpioProviderBase implements GpioProvider {

    public static final String NAME = "com.pi4j.gpio.extension.tca.TCA6424AGpioProvider";
    public static final String DESCRIPTION = "TCA6424A GPIO Provider";

    public static final int DEFAULT_ADDRESS = 0x22;
    public static final int DEFAULT_POLLING_TIME = 50;

    public static final int REGISTER_INPUT0 = 0x00;
    public static final int REGISTER_INPUT1 = 0x01;
    public static final int REGISTER_INPUT2 = 0x02;
    public static final int REGISTER_OUTPUT0 = 0x04;
    public static final int REGISTER_OUTPUT1 = 0x05;
    public static final int REGISTER_OUTPUT2 = 0x06;
    public static final int REGISTER_POLARITY0 = 0x08;
    public static final int REGISTER_POLARITY1 = 0x09;
    public static final int REGISTER_POLARITY2 = 0x0A;
    public static final int REGISTER_DIRECTION0 = 0x0C;
    public static final int REGISTER_DIRECTION1 = 0x0D;
    public static final int REGISTER_DIRECTION2 = 0x0E;

    private int currentStates0 = 0;
    private int currentStates1 = 0;
    private int currentStates2 = 0;

    private int currentPolarity0 = 0;
    private int currentPolarity1 = 0;
    private int currentPolarity2 = 0;

    private int currentDirection0 = 0;
    private int currentDirection1 = 0;
    private int currentDirection2 = 0;

    private int pollingTime = DEFAULT_POLLING_TIME;

    private boolean i2cBusOwner = false;
    private I2CBus bus;
    private I2CDevice device;

    public TCA6424AGpioProvider(int busNumber, int address) throws UnsupportedBusNumberException, IOException {
        // create I2C communications bus instance
        this(I2CFactory.getInstance(busNumber), address);
        i2cBusOwner = true;
    }

    public TCA6424AGpioProvider(int busNumber, int address, int pollingTime)
            throws IOException, UnsupportedBusNumberException {
        // create I2C communications bus instance
        this(I2CFactory.getInstance(busNumber), address, pollingTime);
        i2cBusOwner = true;
    }

    public TCA6424AGpioProvider(I2CBus bus, int address) throws IOException {
        this(bus, address, DEFAULT_POLLING_TIME);
    }

    public TCA6424AGpioProvider(I2CBus bus, int address, int pollingTime) throws IOException {
        // set reference to I2C communications bus instance
        this.bus = bus;

        // create I2C device instance
        device = bus.getDevice(address);

        // read initial GPIO pin states
        currentStates0 = device.read(REGISTER_INPUT0);
        currentStates1 = device.read(REGISTER_INPUT1);
        currentStates2 = device.read(REGISTER_INPUT2);

        // set default GPIO
        currentStates0 = device.read(REGISTER_OUTPUT0);
        currentStates1 = device.read(REGISTER_OUTPUT1);
        currentStates2 = device.read(REGISTER_OUTPUT2);

        // set all default pins directions
        currentPolarity0 = device.read(REGISTER_POLARITY0);
        currentPolarity1 = device.read(REGISTER_POLARITY1);
        currentPolarity2 = device.read(REGISTER_POLARITY2);

        // set all default pins directions
        currentDirection0 = device.read(REGISTER_DIRECTION0);
        currentDirection1 = device.read(REGISTER_DIRECTION1);
        currentDirection2 = device.read(REGISTER_DIRECTION2);

        // set pollingtime
        this.pollingTime = pollingTime;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void export(Pin pin, PinMode mode) {
        // make sure to set the pin mode
        super.export(pin, mode);
        setMode(pin, mode);
    }

    @Override
    public void unexport(Pin pin) {
        super.unexport(pin);
        setMode(pin, PinMode.DIGITAL_OUTPUT);
    }

    @Override
    public void setMode(Pin pin, PinMode mode) {
        super.setMode(pin, mode);

        try {
            // determine the bank
            int stateBank = pin.getAddress() / 8;

            // determine pin address
            int pinAddress = pin.getAddress() % 8;

            int states;
            int register;
            switch (stateBank) {
                case 0:
                    states = currentDirection0;
                    register = REGISTER_DIRECTION0;
                    break;
                case 1:
                    states = currentDirection1;
                    register = REGISTER_DIRECTION1;
                    break;
                case 2:
                    states = currentDirection2;
                    register = REGISTER_DIRECTION2;
                    break;
                default:
                    throw new IllegalArgumentException("stateBank = " + stateBank);
            }

            // determine update direction value based on mode
            if (mode == PinMode.DIGITAL_INPUT) {
                states |= pinAddress;
            } else if (mode == PinMode.DIGITAL_OUTPUT) {
                states &= ~pinAddress;
            }

            // update state value
            device.write(register, (byte) states);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public PinMode getMode(Pin pin) {
        return super.getMode(pin);
    }

    @Override
    public void setState(Pin pin, PinState state) {
        super.setState(pin, state);

        try {
            // determine the bank
            int stateBank = pin.getAddress() / 8;

            // determine pin address
            int pinAddress = pin.getAddress() % 8;

            int states;
            int register;
            switch (stateBank) {
                case 0:
                    states = currentStates0;
                    register = REGISTER_INPUT0;
                    break;
                case 1:
                    states = currentStates1;
                    register = REGISTER_INPUT1;
                    break;
                case 2:
                    states = currentStates2;
                    register = REGISTER_INPUT2;
                    break;
                default:
                    throw new IllegalArgumentException("stateBank = " + stateBank);
            }

            // determine state value for pin bit
            if (state.isHigh()) {
                states |= pinAddress;
            } else {
                states &= ~pinAddress;
            }

            // update state value
            device.write(register, (byte) states);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public PinState getState(Pin pin) {
        // call super method to perform validation on pin
        PinState result = super.getState(pin);

        // determine the bank
        int stateBank = pin.getAddress() / 8;

        // determine pin address
        int pinAddress = pin.getAddress() % 8;

        int states;
        switch (stateBank) {
            case 0:
                states = currentStates0;
                break;
            case 1:
                states = currentStates1;
                break;
            case 2:
                states = currentStates2;
                break;
            default:
                throw new IllegalArgumentException("stateBank = " + stateBank);
        }

        // determine pin state
        PinState state = (states & pinAddress) == pinAddress ? PinState.HIGH : PinState.LOW;

        // cache state
        getPinCache(pin).setState(state);

        return state;
    }

    @Override
    public void shutdown() {
        // prevent reentrant invocation
        if (isShutdown()) {
            return;
        }

        // perform shutdown login in base
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
