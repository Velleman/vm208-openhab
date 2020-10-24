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
package org.openhab.binding.vm208.internal;

import static org.openhab.binding.vm208.internal.VM208BindingConstants.*;
import static org.openhab.binding.vm208.internal.i2c.TCA6424APin.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.vm208.internal.i2c.TCA6424AProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * The {@link VM208ExHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
public class VM208ExHandler extends BaseThingHandler implements VM208BaseHandler, VM208Ex {

    private final Logger logger = LoggerFactory.getLogger(VM208ExHandler.class);

    private @Nullable VM208ExConfiguration config;

    private @Nullable VM208IntHandler gateway;

    private @Nullable TCA6424AProvider tcaProvider;

    private int socket;
    private boolean ledReflectsRelayStatus;

    private static final Pin[] RELAY_PIN_MAP = new Pin[] { TCA6424A_P00, TCA6424A_P01, TCA6424A_P02, TCA6424A_P03,
            TCA6424A_P04, TCA6424A_P05, TCA6424A_P06, TCA6424A_P07 };

    private static final Pin[] BUTTON_PIN_MAP = new Pin[] { TCA6424A_P10, TCA6424A_P11, TCA6424A_P12, TCA6424A_P13,
            TCA6424A_P14, TCA6424A_P15, TCA6424A_P16, TCA6424A_P17 };

    private static final Pin[] LED_PIN_MAP = new Pin[] { TCA6424A_P20, TCA6424A_P21, TCA6424A_P22, TCA6424A_P23,
            TCA6424A_P24, TCA6424A_P25, TCA6424A_P26, TCA6424A_P27 };

    public VM208ExHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        gateway = (VM208IntHandler) this.getBridge().getHandler();

        tcaProvider = initializeTcaProvider(gateway.getBusNumber(), gateway.getAddress());

        updateStatus(ThingStatus.ONLINE);
    }

    private @Nullable TCA6424AProvider initializeTcaProvider(int busNumber, int address) {
        TCA6424AProvider tca = null;
        logger.debug("initializing tca provider for busNumber {} and address {}", busNumber, address);
        try {
            tca = new TCA6424AProvider(busNumber, address);
        } catch (UnsupportedBusNumberException | IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Tried to access not available I2C bus: " + ex.getMessage());
        }
        logger.debug("got tcaProvider {}", tca);
        return tca;
    }

    protected void checkConfiguration() {
        config = getConfigAs(VM208ExConfiguration.class);
        socket = config.getSocket();
        ledReflectsRelayStatus = config.isLedReflectsRelayStatus();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command: {} on channelGroup {} on channel {}", command.toFullString(),
                channelUID.getGroupId(), channelUID.getIdWithoutGroup());

        String[] relayChannels = new String[] { RELAY_1, RELAY_2, RELAY_3, RELAY_4, RELAY_5, RELAY_6, RELAY_7,
                RELAY_8 };

        for (int i = 1; i < relayChannels.length; i++) {
            String relayChannel = relayChannels[i];
            if (relayChannel.equals(channelUID.getGroupId())) {
                switch (channelUID.getIdWithoutGroup()) {
                    case RELAY:
                        if (command instanceof OnOffType) {
                            if (command.equals(OnOffType.ON)) {
                                this.turnRelayOn(i - 1);
                            } else {
                                this.turnRelayOff(i - 1);
                            }
                        }
                        break;
                    case BUTTON:
                        break;
                    case LED:
                        if (command.equals(OnOffType.ON)) {
                            this.turnLedOn(i - 1);
                        } else {
                            this.turnLedOff(i - 1);
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void turnRelayOn(int channel) {
        // request communication
        this.gateway.sendToSocket(this, () -> {
            this.turnRelayOn(channel);

            if (ledReflectsRelayStatus) {
                this.turnLedOn(channel);
            }
        });
    }

    private void turnRelayOnWithoutLock(int channel) {
        // turn relay on
        Pin pin = VM208ExHandler.RELAY_PIN_MAP[channel];
        PinState pinState = PinState.HIGH;
        this.tcaProvider.setState(pin, pinState);
    }

    @Override
    public void turnRelayOff(int channel) {
        // request communication
        this.gateway.sendToSocket(this, () -> {
            this.turnRelayOffWithoutLock(channel);

            if (ledReflectsRelayStatus) {
                this.turnLedOff(channel);
            }
        });
    }

    private void turnRelayOffWithoutLock(int channel) {
        // turn relay off
        Pin pin = VM208ExHandler.RELAY_PIN_MAP[channel];
        PinState pinState = PinState.LOW;
        this.tcaProvider.setState(pin, pinState);
    }

    @Override
    public void turnLedOn(int channel) {
        // request communication
        this.gateway.sendToSocket(this, () -> {
            this.turnLedOnWithoutLock(channel);
        });
    }

    private void turnLedOnWithoutLock(int channel) {
        // turn led on
        Pin pin = VM208ExHandler.LED_PIN_MAP[channel];
        PinState pinState = PinState.HIGH;
        this.tcaProvider.setState(pin, pinState);
    }

    @Override
    public void turnLedOff(int channel) {
        // request communication
        this.gateway.sendToSocket(this, () -> {
            this.turnLedOffWithoutLock(channel);
        });
    }

    private void turnLedOffWithoutLock(int channel) {
        // turn led off
        Pin pin = VM208ExHandler.LED_PIN_MAP[channel];
        PinState pinState = PinState.LOW;
        this.tcaProvider.setState(pin, pinState);
    }

    @Override
    public void isButtonPressed(int channel) {
        // request communication
        this.gateway.sendToSocket(this, () -> {
            this.isButtonPressedWithoutLock(channel);
        });
    }

    private void isButtonPressedWithoutLock(int channel) {
        // get button state
        Pin pin = VM208ExHandler.BUTTON_PIN_MAP[channel];
        PinState pinState = PinState.HIGH; // active low so result is inverted
        this.tcaProvider.setState(pin, pinState);
        // !this->_tca->readPin(this->_id + TCA6424A_P10); //Active low so invert the result;
    }

    @Override
    public int getSocket() {
        return socket;
    }
}
