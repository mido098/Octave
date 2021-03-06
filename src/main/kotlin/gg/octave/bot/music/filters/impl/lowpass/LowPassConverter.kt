/*
 * MIT License
 *
 * Copyright (c) 2020 Melms Media LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package gg.octave.bot.music.filters.impl.lowpass

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter

open class LowPassConverter(private val downstream: FloatPcmAudioFilter, channelCount: Int) : FloatPcmAudioFilter {
    var smoothingFactor = 20f

    private val values = FloatArray(channelCount)
    private var initialized = false

    override fun process(input: Array<out FloatArray>, offset: Int, length: Int) {
        if (!initialized) {
            for (c in input.indices) {
                values[c] = input[c][offset]
            }

            initialized = true
        }

        for (c in input.indices) {
            var value = values[c]

            for (i in offset until offset + length) {
                val currentValue = input[c][i]
                value += (currentValue - value) / smoothingFactor
                input[c][i] = value
            }

            values[c] = value
        }

        downstream.process(input, offset, length)
    }

    override fun seekPerformed(requestedTime: Long, providedTime: Long) {
        initialized = false
    }

    override fun flush() {

    }

    override fun close() {

    }
}
