/*
 * MIT License
 *
 * Copyright (c) 2021-2023 Jannis Weis
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
package com.github.weisj.jsvg_mc.attributes.stroke;

import java.awt.*;

import org.intellij.lang.annotations.MagicConstant;

import com.github.weisj.jsvg_mc.attributes.Default;

public enum LineJoin {
    @Default
    Miter(BasicStroke.JOIN_MITER),
    Round(BasicStroke.JOIN_ROUND),
    Bevel(BasicStroke.JOIN_BEVEL);

    private final int awtCode;

    LineJoin(int awtCode) {
        this.awtCode = awtCode;
    }

    @MagicConstant(
        intValues = {BasicStroke.JOIN_BEVEL, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_MITER}
    )
    public int awtCode() {
        return awtCode;
    }
}
