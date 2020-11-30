/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.vm208.internal.i2c;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * The {@link TCA6424AProvider} implements the TCA6424A chip.
 * The datasheet is available at:
 * https://www.ti.com/lit/ds/symlink/tca6424a.pdf
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
public class TCA6424AProvider {

    private final Logger logger = LoggerFactory.getLogger(TCA6424AProvider.class);

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

    private int currentInputStates0 = 0;
    private int currentInputStates1 = 0;
    private int currentInputStates2 = 0;

    private int currentOutputStates0 = 0;
    private int currentOutputStates1 = 0;
    private int currentOutputStates2 = 0;

    @SuppressWarnings("unused")
    private int currentPolarity0 = 0;
    @SuppressWarnings("unused")
    private int currentPolarity1 = 0;
    @SuppressWarnings("unused")
    private int currentPolarity2 = 0;

    private int currentDirection0 = 0;
    private int currentDirection1 = 0;
    private int currentDirection2 = 0;

    private boolean i2cBusOwner = false;
    private I2CBus bus;
    private I2CDevice device;

    public TCA6424AProvider(int busNumber, int address) throws UnsupportedBusNumberException, IOException {
        // create I2C communications bus instance
        this(I2CFactory.getInstance(busNumber), address);
        i2cBusOwner = true;
    }

    public TCA6424AProvider(int busNumber, int address, int pollingTime)
            throws IOException, UnsupportedBusNumberException {
        // create I2C communications bus instance
        this(I2CFactory.getInstance(busNumber), address, pollingTime);
        i2cBusOwner = true;
    }

    public TCA6424AProvider(I2CBus bus, int address) throws IOException {
        this(bus, address, DEFAULT_POLLING_TIME);
    }

    public TCA6424AProvider(I2CBus bus, int address, int pollingTime) throws IOException {
        // set reference to I2C communications bus instance
        this.bus = bus;

        // create I2C device instance
        device = bus.getDevice(address);
    }

    public boolean setDirectionSettings(int direction0, int direction1, int direction2) {
        boolean wroteNewSettings = false;

        try {
            readSettings();

            if (currentDirection0 != direction0) {
                writeToDevice(REGISTER_DIRECTION0, (byte) direction0);
                currentDirection0 = direction0;
                wroteNewSettings = true;
            }
            if (currentDirection1 != direction1) {
                writeToDevice(REGISTER_DIRECTION1, (byte) direction1);
                currentDirection1 = direction1;
                wroteNewSettings = true;
            }
            if (currentDirection2 != direction2) {
                writeToDevice(REGISTER_DIRECTION2, (byte) direction2);
                currentDirection2 = direction2;
                wroteNewSettings = true;
            }
        } catch (Exception ex) {
            logger.error("{}", ex.toString());
        }

        return wroteNewSettings;
    }

    public void setOutput0Settings(int output0) {
        try {
            writeToDevice(REGISTER_OUTPUT0, (byte) output0);
            currentOutputStates0 = output0;
        } catch (Exception ex) {
            logger.error("{}", ex.toString());
        }
    }

    public void setOutput1Settings(int output1) {
        try {
            writeToDevice(REGISTER_OUTPUT1, (byte) output1);
            currentOutputStates1 = output1;
        } catch (Exception ex) {
            logger.error("{}", ex.toString());
        }
    }

    public void setOutput2Settings(int output2) {
        try {
            writeToDevice(REGISTER_OUTPUT2, (byte) output2);
            currentOutputStates2 = output2;
        } catch (Exception ex) {
            logger.error("{}", ex.toString());
        }
    }

    public void readSettings() {
        try {
            // set all default pins polarities
            currentPolarity0 = readFromDevice(REGISTER_POLARITY0);
            currentPolarity1 = readFromDevice(REGISTER_POLARITY1);
            currentPolarity2 = readFromDevice(REGISTER_POLARITY2);

            // set all default pins directions
            currentDirection0 = readFromDevice(REGISTER_DIRECTION0);
            currentDirection1 = readFromDevice(REGISTER_DIRECTION1);
            currentDirection2 = readFromDevice(REGISTER_DIRECTION2);
        } catch (IOException ex) {
            logger.error("{}", ex.toString());
        }
    }

    public void readStates() {
        try {
            // read initial GPIO pin states
            currentInputStates0 = readFromDevice(REGISTER_INPUT0);
            currentInputStates1 = readFromDevice(REGISTER_INPUT1);
            currentInputStates2 = readFromDevice(REGISTER_INPUT2);

            // set default GPIO
            currentOutputStates0 = readFromDevice(REGISTER_OUTPUT0);
            currentOutputStates1 = readFromDevice(REGISTER_OUTPUT1);
            currentOutputStates2 = readFromDevice(REGISTER_OUTPUT2);
        } catch (IOException ex) {
            logger.error("{}", ex.toString());
        }
    }

    public String getName() {
        return NAME;
    }

    public void export(@Nullable Pin pin, @Nullable PinMode mode) {
        // make sure to set the pin mode
        setMode(pin, mode);
    }

    public void unexport(@Nullable Pin pin) {
        setMode(pin, PinMode.DIGITAL_OUTPUT);
    }

    public PinMode getMode(Pin pin) {
        // determine the bank
        int stateBank = pin.getAddress() / 8;

        // determine pin address
        int pinAddress = (pin.getAddress() % 8) + 1;

        // configured as input or output

        int states;
        switch (stateBank) {
            case 0:
                states = currentDirection0;
                break;
            case 1:
                states = currentDirection1;
                break;
            case 2:
                states = currentDirection2;
                break;
            default:
                throw new IllegalArgumentException("stateBank = " + stateBank);
        }

        return ((states >> (pinAddress - 1)) & 1) == 1 ? PinMode.DIGITAL_INPUT : PinMode.DIGITAL_OUTPUT;
    }

    public void setMode(@Nullable Pin pin, @Nullable PinMode mode) {
        if (pin == null || mode == null) {
            return;
        }

        try {
            // determine the bank
            int stateBank = pin.getAddress() / 8;

            // determine pin address
            int pinAddress = (pin.getAddress() % 8) + 1;

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
                states |= 1 << (pinAddress - 1);
            } else if (mode == PinMode.DIGITAL_OUTPUT) {
                states &= ~(1 << (pinAddress - 1));
            }

            // update state value
            writeToDevice(register, (byte) states);
            register = states;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public PinState getState(Pin pin) {
        // determine the bank
        int stateBank = pin.getAddress() / 8;

        // determine pin address
        int pinAddress = (pin.getAddress() % 8) + 1;

        // configured as input or output
        boolean isInput = getMode(pin) == PinMode.DIGITAL_INPUT;

        int states;
        switch (stateBank) {
            case 0:
                states = isInput ? currentInputStates0 : currentOutputStates0;
                break;
            case 1:
                states = isInput ? currentInputStates1 : currentOutputStates1;
                break;
            case 2:
                states = isInput ? currentInputStates2 : currentOutputStates2;
                break;
            default:
                throw new IllegalArgumentException("stateBank = " + stateBank);
        }

        return ((states >> (pinAddress - 1)) & 1) == 0 ? PinState.LOW : PinState.HIGH;
    }

    public void setState(@Nullable Pin pin, @Nullable PinState state) {
        if (pin == null || state == null) {
            return;
        }

        try {
            // determine the bank
            int stateBank = pin.getAddress() / 8;

            // determine pin address
            int pinAddress = (pin.getAddress() % 8) + 1;

            int states;
            int register;
            switch (stateBank) {
                case 0:
                    states = currentOutputStates0;
                    register = REGISTER_OUTPUT0;
                    break;
                case 1:
                    states = currentOutputStates1;
                    register = REGISTER_OUTPUT1;
                    break;
                case 2:
                    states = currentOutputStates2;
                    register = REGISTER_OUTPUT2;
                    break;
                default:
                    throw new IllegalArgumentException("stateBank = " + stateBank);
            }

            // determine state value for pin bit
            if (state.isHigh()) {
                states |= 1 << (pinAddress - 1);
            } else {
                states &= ~(1 << (pinAddress - 1));
            }

            // update state value
            writeToDevice(register, (byte) states);

            switch (stateBank) {
                case 0:
                    currentOutputStates0 = states;
                    break;
                case 1:
                    currentOutputStates1 = states;
                    break;
                case 2:
                    currentOutputStates2 = states;
                    break;
                default:
                    throw new IllegalArgumentException("stateBank = " + stateBank);
            }
        } catch (IOException | IllegalArgumentException ex) {
            logger.error("{}", ex.toString());
        }
    }

    private void writeToDevice(int register, byte states) throws IOException {
        logger.debug("0x{} >> (write) 0x{} to 0x{}", HexUtils.toHex(device.getAddress()), HexUtils.toHex(states),
                HexUtils.toHex(register));
        device.write(register, states);
    }

    private int readFromDevice(int register) throws IOException {
        int result = device.read(register);
        logger.debug("0x{} >> (read) 0x{} from 0x{}", HexUtils.toHex(device.getAddress()), HexUtils.toHex(result),
                HexUtils.toHex(register));
        return result;
    }

    public void shutdown() {
        try {
            // if we are the owner of the I2C bus, then close it
            if (i2cBusOwner) {
                // close the I2C bus communication
                bus.close();
            }
        } catch (IOException ex) {
            logger.error("{}", ex.toString());
        }
    }
}
