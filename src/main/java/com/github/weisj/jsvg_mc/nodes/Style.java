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
package com.github.weisj.jsvg_mc.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.github.weisj.jsvg_mc.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg_mc.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg_mc.parser.css.CssParser;
import com.github.weisj.jsvg_mc.parser.css.StyleSheet;

@ElementCategories({/* None */})
@PermittedContent(any = true, charData = true)
public final class Style extends MetaSVGNode {
    public static final String TAG = "style";

    private StyleSheet styleSheet;

    private final List<char[]> data = new ArrayList<>();

    public void parseStyleSheet(@NotNull CssParser cssParser) {
        styleSheet = cssParser.parse(data);
        data.clear();
    }

    public @NotNull StyleSheet styleSheet() {
        return Objects.requireNonNull(styleSheet);
    }

    @Override
    public void addContent(char[] content) {
        data.add(content);
    }

    @Override
    public @NotNull String tagName() {
        return TAG;
    }

}
