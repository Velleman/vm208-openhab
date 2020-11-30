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
package org.openhab.binding.vm208.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.vm208.internal.i2c.GPIODataHolder;
import org.openhab.binding.vm208.internal.i2c.TCA9544Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * The {@link VM208IntHandler} is responsible as gateway for 4 modules.
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
public class VM208IntHandler extends BaseBridgeHandler implements GpioPinListenerDigital {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final VM208BusHandler bus;

    private @NonNullByDefault({}) VM208IntConfiguration config;

    private int busNumber;
    private int address;
    private int interruptPin;

    private VM208BaseHandler[] sockets;

    private @NonNullByDefault({}) TCA9544Provider tcaProvider;

    private @Nullable GpioPinDigitalInput interruptPinInput;

    public VM208IntHandler(VM208BusHandler bus, Bridge bridge) {
        super(bridge);
        this.bus = bus;

        this.sockets = new VM208BaseHandler[4];
    }

    public int getBusNumber() {
        return busNumber;
    }

    public int getAddress() {
        return address;
    }

    public void registerSocket(VM208BaseHandler vm208baseHandler) {
        int socket = vm208baseHandler.getSocket();
        if (this.sockets[socket - 1] == null) {
            this.sockets[socket - 1] = vm208baseHandler;
        } else {
            throw new IllegalStateException("Socket " + socket + " has been registered before.");
        }
    }

    public void unregisterSocket(VM208BaseHandler vm208baseHandler) {
        int socket = vm208baseHandler.getSocket();
        if (this.sockets[socket - 1] == null) {
            throw new IllegalStateException("Socket " + socket + " has not been registered.");
        } else {
            this.sockets[socket - 1] = null;
        }
    }

    @Override
    public void initialize() {
        try {
            checkConfiguration();
            tcaProvider = initializeTcaProvider();
            interruptPinInput = initializeInterruptPin();
            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException | SecurityException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "An exception occurred while adding pin. Check pin configuration. Exception: " + e.getMessage());
        }
    }

    private @Nullable GpioPinDigitalInput initializeInterruptPin() {
        logger.debug("Initializing interrupt pin for thing {}", thing.getUID().getAsString());
        @Nullable
        Pin pin = RaspiPin.getPinByAddress(this.interruptPin);
        if (pin == null) {
            logger.debug("No pin found for {}", this.interruptPin);
            return null;
        }
        logger.debug("Initializing pin {}", pin);

        GpioPinDigitalInput input = GPIODataHolder.GPIO.provisionDigitalInputPin(pin,
                "InterruptPin" + this.interruptPin);
        input.addListener(this);

        logger.debug("Bound digital input for PIN: {}", pin);
        return input;
    }

    private @Nullable TCA9544Provider initializeTcaProvider() {
        TCA9544Provider tca = null;
        logger.debug("Initializing tca provider for busNumber {} and address {}", busNumber, address);
        try {
            tca = new TCA9544Provider(busNumber, address);
        } catch (UnsupportedBusNumberException | IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Tried to access not available I2C bus: " + ex.getMessage());
        }
        logger.debug("Got tcaProvider {}", tca);
        return tca;
    }

    protected void checkConfiguration() {
        config = getConfigAs(VM208IntConfiguration.class);
        address = Integer.parseInt(Integer.toString(config.getAddress()), 16);
        busNumber = config.getBusNumber();
        interruptPin = config.getInterruptPin();
    }

    // Only one socket can access this method at a time
    public synchronized void sendToSocket(VM208BaseHandler vm208baseHandler, Runnable command) {
        int socket = vm208baseHandler.getSocket();

        // Only one interface can communicate with the bus,
        // since each device has the same address
        bus.claimBus(() -> {
            boolean channelHasChanged = false;
            try {
                tcaProvider.changeChannel((byte) socket);
                channelHasChanged = true;

                command.run();
            } catch (IOException ex) {
                logger.error("", ex);
            } finally {
                if (channelHasChanged) {
                    try {
                        tcaProvider.changeChannel((byte) 0);
                    } catch (IOException ex) {
                        logger.error("", ex);
                    }
                }
            }
        });
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(@Nullable GpioPinDigitalStateChangeEvent event) {
        if (event != null) {
            // Only pass through high events
            if (event.getState() != PinState.LOW) {
                return;
            }

            try {
                int interrupt = tcaProvider.readInterrupts();
                // Fetch an update for every connected socket
                if (interrupt != 0) {
                    for (int i = 0; i < this.sockets.length; i++) {
                        boolean hasInterrupt = ((interrupt >> i) & 1) == 1;
                        if (hasInterrupt) {
                            VM208BaseHandler socket = this.sockets[i];
                            logger.debug("Handling interrupt on socket {}", i + 1);
                            if (socket != null) {
                                socket.fetchUpdate();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("", ex);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no channels
    }

    @Override
    public void dispose() {
        super.dispose();

        if (interruptPinInput != null) {
            GPIODataHolder.GPIO.unprovisionPin(interruptPinInput);
        }

        if (tcaProvider != null) {
            tcaProvider.shutdown();
            tcaProvider = null;
        }
    }
}
