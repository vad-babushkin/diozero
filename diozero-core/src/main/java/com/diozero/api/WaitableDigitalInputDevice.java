package com.diozero.api;

/*
 * #%L
 * Device I/O Zero - Core
 * %%
 * Copyright (C) 2016 diozero
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.diozero.util.RuntimeIOException;

/**
 * Represents a generic input device with distinct waitable states.
 * 
 * This class extends 'InputDevice' with methods for waiting on the device's
 * status ('wait_for_active' and 'wait_for_inactive'), and properties that hold
 * functions to be called when the device changes state ('when_activated' and
 * 'when_deactivated'). These are aliased appropriately in various subclasses.
 * 
 * Note that this class provides no means of actually firing its events; it's
 * effectively an abstract base class.
 */
public class WaitableDigitalInputDevice extends DigitalInputDevice {
	protected DigitalInputEvent lastPinEvent;

	public WaitableDigitalInputDevice(int pinNumber, GpioPullUpDown pud, GpioEventTrigger trigger) throws RuntimeIOException {
		super(pinNumber, pud, trigger);
	}

	public void waitForActive() throws InterruptedException {
		waitForActive(0);
	}

	public void waitForActive(int timeout) throws InterruptedException {
		waitForValue(activeHigh, timeout);
	}

	public void waitForInactive() throws InterruptedException {
		waitForInactive(0);
	}

	public void waitForInactive(int timeout) throws InterruptedException {
		waitForValue(!activeHigh, timeout);
	}

	protected void waitForValue(boolean value, int timeout) throws InterruptedException {
		long start = System.currentTimeMillis();
		long wait_for_millis = timeout;
		synchronized (this) {
			do {
				if (wait_for_millis > 0) {
					wait(wait_for_millis);
				} else {
					wait();
				}
				if (lastPinEvent.getValue() == value) {
					// Got the value we were waiting for, exit the loop
					break;
				}
				wait_for_millis = timeout - (System.currentTimeMillis() - start);
			} while (wait_for_millis > 0);
		}
	}

	@Override
	public void valueChanged(DigitalInputEvent event) {
		synchronized (this) {
			lastPinEvent = event;
			notify();
		}
		// Notify any listeners
		super.valueChanged(event);
	}
}
