/*
 * MIT License
 *
 * Copyright (c) 2024 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.weisj.jsvg_mc.geometry.size;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public final class Angle {
    public static final float UNSPECIFIED_RAW = Float.NaN;
    public static final @NotNull Angle UNSPECIFIED = new Angle(AngleUnit.Raw, UNSPECIFIED_RAW);
    public static final @NotNull Angle ZERO = new Angle(AngleUnit.Raw, 0);

    private final float radian;

    public Angle(AngleUnit unit, float value) {
        this.radian = unit.toRadians(value);
    }

    public static boolean isUnspecified(float value) {
        return Float.isNaN(value);
    }

    public static boolean isSpecified(float value) {
        return !isUnspecified(value);
    }

    public float radians() {
        return radian;
    }

    public boolean isUnspecified() {
        return isUnspecified(radian);
    }

    public boolean isSpecified() {
        return !isUnspecified();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Angle angle = (Angle) o;
        return Float.compare(radian, angle.radian) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(radian);
    }

    @Override
    public String toString() {
        return "Angle{" +
                "radian=" + radian +
                '}';
    }
}
