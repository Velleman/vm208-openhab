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

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * The {@link TCA6424AProvider} implements the TCA6424A chip.
 * The datasheet is available at:
 * https://www.ti.com/lit/ds/symlink/tca9544a.pdf
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
public class TCA9544Provider {

    public static final String NAME = "com.pi4j.extension.tca.TCA9544Provider";
    public static final String DESCRIPTION = "TCA9544 Provider";

    public static final int DEFAULT_POLLING_TIME = 50;

    public static final int REGISTER_CONTROL = 0x00;

    private int currentStates = 0;

    private boolean i2cBusOwner = false;
    private I2CBus bus;
    private I2CDevice device;

    public TCA9544Provider(int busNumber, int address) throws UnsupportedBusNumberException, IOException {
        // create I2C communications bus instance
        this(I2CFactory.getInstance(busNumber), address, DEFAULT_POLLING_TIME);

        // create I2C device instance
        device = bus.getDevice(address);

        i2cBusOwner = true;
    }

    public TCA9544Provider(I2CBus bus, int address, int pollingTime) throws IOException {
        // set reference to I2C communications bus instance
        this.bus = bus;

        // create I2C device instance
        device = bus.getDevice(address);
    }

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

    public void shutdown() {
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
