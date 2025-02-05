package com.diozero.devices.oled;

import org.tinylog.Logger;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     SsdOledCommunicationChannel.java
 *
 * This file is part of the diozero project. More information about this project
 * can be found at https://www.diozero.com/.
 * %%
 * Copyright (C) 2016 - 2023 diozero
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

import com.diozero.api.I2CDevice;
import com.diozero.api.SpiDevice;

/**
 * Comms for OLED devices.
 */
public interface SsdOledCommunicationChannel extends AutoCloseable {
	void write(byte... data);
	void write(byte[] buffer, int offset, int length);
	@Override
	void close();

	/**
	 * SPI channel
	 * TODO move DC Pin and reset Pin to this channel since not used for I2C?
	 */
	class SpiCommunicationChannel implements SsdOledCommunicationChannel {
		private final SpiDevice device;

		public SpiCommunicationChannel(int chipSelect, int controller, int spiFrequency) {
			device = SpiDevice.builder(chipSelect).setController(controller).setFrequency(spiFrequency).build();
		}

		@Override
		public void write(byte... data) {
			device.write(data);
		}

		@Override
		public void write(byte[] txBuffer, int txOffset, int length) {
			device.write(txBuffer, txOffset, length);
		}

		@Override
		public void close() {
			Logger.trace("close()");
			device.close();
		}
	}

	/**
	 * I2C channel.
	 * <p>
	 * TODO Check I2C transaction size limit - i2c specification does not state - Tested up to 4K bytes
	 */
	class I2cCommunicationChannel implements SsdOledCommunicationChannel {
		private final I2CDevice device;

		public I2cCommunicationChannel(I2CDevice device) {
			this.device = device;
		}

		@Override
		public void write(byte... commands) {
			device.writeBytes(commands);
		}

		@Override
		public void write(byte[] buffer, int offset, int length) {
			byte[] data = new byte[length];
			System.arraycopy(buffer, offset, data, 0, length);
			device.writeBytes(data);
		}

		@Override
		public void close() {
			Logger.trace("close()");
			device.close();
		}
	}
}
