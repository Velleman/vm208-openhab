package org.openhab.binding.vm208.internal.i2c;

import java.io.IOException;

import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.GpioProviderBase;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.PinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.PinListener;
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
    private GpioStateMonitor monitor = null;

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

            // if any pins are configured as input pins, then we need to start the interrupt monitoring
            // thread
            if (currentDirection0 > 0 || currentDirection1 > 0 || currentDirection2 > 0) {
                // if the monitor has not been started, then start it now
                if (monitor == null) {
                    // start monitoring thread
                    monitor = new GpioStateMonitor(device);
                    monitor.start();
                }
            } else {
                // shutdown and destroy monitoring thread since there are no input pins configured
                if (monitor != null) {
                    monitor.shutdown();
                    monitor = null;
                }
            }
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
            // if a monitor is running, then shut it down now
            if (monitor != null) {
                // shutdown monitoring thread
                monitor.shutdown();
                monitor = null;
            }

            // if we are the owner of the I2C bus, then close it
            if (i2cBusOwner) {
                // close the I2C bus communication
                bus.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This class/thread is used to to actively monitor for GPIO interrupts
     *
     * @author Robert Savage
     *
     */
    private class GpioStateMonitor extends Thread {
        private I2CDevice device;
        private boolean shuttingDown = false;

        public GpioStateMonitor(I2CDevice device) {
            this.device = device;
        }

        public void shutdown() {
            shuttingDown = true;
        }

        @Override
        public void run() {
            while (!shuttingDown) {
                try {
                    // only process for interrupts if a pin is configured as an input pin
                    if (currentDirection0 > 0) {
                        // process interrupts
                        int pinInterrupt = this.device.read(REGISTER_INTF);

                        // validate that there is at least one interrupt active
                        if (pinInterrupt > 0) {
                            // read the current pin states
                            int pinInterruptState = device.read(REGISTER_GPIO);

                            // loop over the available pins
                            for (Pin pin : TCA6424APin.ALL) {
                                // is there an interrupt flag on this pin?
                                // if ((pinInterrupt & pin.getAddress()) > 0) {
                                // System.out.println("INTERRUPT ON PIN [" + pin.getName() + "]");
                                evaluatePinForChange(pin, pinInterruptState);
                                // }
                            }
                        }
                    }

                    // ... lets take a short breather ...
                    Thread.currentThread();
                    Thread.sleep(pollingTime);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void evaluatePinForChange(Pin pin, int state) {
            if (getPinCache(pin).isExported()) {
                // determine pin address
                int pinAddress = pin.getAddress();

                if ((state & pinAddress) != (currentStates & pinAddress)) {
                    PinState newState = (state & pinAddress) == pinAddress ? PinState.HIGH : PinState.LOW;

                    // cache state
                    getPinCache(pin).setState(newState);

                    // determine and cache state value for pin bit
                    if (newState.isHigh()) {
                        currentStates |= pinAddress;
                    } else {
                        currentStates &= ~pinAddress;
                    }

                    // change detected for INPUT PIN
                    // System.out.println("<<< CHANGE >>> " + pin.getName() + " : " + state);
                    dispatchPinChangeEvent(pin.getAddress(), newState);
                }
            }
        }

        private void dispatchPinChangeEvent(int pinAddress, PinState state) {
            // iterate over the pin listeners map
            for (Pin pin : listeners.keySet()) {
                // System.out.println("<<< DISPATCH >>> " + pin.getName() + " : " +
                // state.getName());

                // dispatch this event to the listener
                // if a matching pin address is found
                if (pin.getAddress() == pinAddress) {
                    // dispatch this event to all listener handlers
                    for (PinListener listener : listeners.get(pin)) {
                        listener.handlePinEvent(new PinDigitalStateChangeEvent(this, pin, state));
                    }
                }
            }
        }
    }
}
