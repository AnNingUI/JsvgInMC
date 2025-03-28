/*
 * MIT License
 *
 * Copyright (c) 2021-2025 Jannis Weis
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
package com.github.weisj.jsvg_mc.nodes.text;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;

import org.jetbrains.annotations.NotNull;

import com.github.weisj.jsvg_mc.attributes.FillRule;
import com.github.weisj.jsvg_mc.attributes.text.GlyphRenderMethod;
import com.github.weisj.jsvg_mc.attributes.text.Side;
import com.github.weisj.jsvg_mc.attributes.text.Spacing;
import com.github.weisj.jsvg_mc.attributes.value.PercentageDimension;
import com.github.weisj.jsvg_mc.geometry.SVGShape;
import com.github.weisj.jsvg_mc.geometry.size.Length;
import com.github.weisj.jsvg_mc.geometry.size.MeasureContext;
import com.github.weisj.jsvg_mc.geometry.util.ReversePathIterator;
import com.github.weisj.jsvg_mc.nodes.Anchor;
import com.github.weisj.jsvg_mc.nodes.ShapeNode;
import com.github.weisj.jsvg_mc.nodes.animation.Animate;
import com.github.weisj.jsvg_mc.nodes.animation.AnimateTransform;
import com.github.weisj.jsvg_mc.nodes.animation.Set;
import com.github.weisj.jsvg_mc.nodes.prototype.spec.Category;
import com.github.weisj.jsvg_mc.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg_mc.nodes.prototype.spec.NotImplemented;
import com.github.weisj.jsvg_mc.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg_mc.parser.AttributeNode;
import com.github.weisj.jsvg_mc.parser.AttributeNode.ElementRelation;
import com.github.weisj.jsvg_mc.renderer.Output;
import com.github.weisj.jsvg_mc.renderer.RenderContext;
import com.github.weisj.jsvg_mc.util.PathUtil;

@ElementCategories({Category.Graphic, Category.TextContent, Category.TextContentChild})
@PermittedContent(
    categories = {Category.Descriptive},
    anyOf = {Anchor.class, TextSpan.class, Animate.class, AnimateTransform.class, Set.class, /* <altGlyph>, <tref> */},
    charData = true
)
public final class TextPath extends TextContainer {
    public static final String TAG = "textpath";
    private static final boolean DEBUG = false;

    private SVGShape pathShape;

    @SuppressWarnings("UnusedVariable")
    private @NotImplemented Spacing spacing;
    @SuppressWarnings("UnusedVariable")
    private @NotImplemented GlyphRenderMethod renderMethod;
    private Side side;

    private Length startOffset;

    @Override
    public @NotNull String tagName() {
        return TAG;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        renderMethod = attributeNode.getEnum("method", GlyphRenderMethod.Align);
        side = attributeNode.getEnum("side", Side.Left);
        spacing = attributeNode.getEnum("spacing", Spacing.Auto);
        // Todo: Needs to be resolved w.r.t to the paths coordinate system
        startOffset = attributeNode.getLength("startOffset", PercentageDimension.CUSTOM, 0);

        String pathData = attributeNode.getValue("path");
        if (pathData != null) {
            pathShape = PathUtil.parseFromPathData(pathData, FillRule.EvenOdd);
        } else {
            String href = attributeNode.getHref();
            ShapeNode shaped =
                    attributeNode.getElementByHref(ShapeNode.class, Category.Shape /* BasicShape or Path */, href,
                            ElementRelation.GEOMETRY_DATA);
            if (shaped != null) {
                pathShape = shaped.shape();
            }
        }
    }

    @Override
    public boolean isVisible(@NotNull RenderContext context) {
        return isValid(context) && super.isVisible(context);
    }

    @Override
    public boolean isValid(@NotNull RenderContext currentContext) {
        return pathShape != null;
    }

    @Override
    protected @NotNull Shape glyphShape(@NotNull RenderContext context) {
        MutableGlyphRun glyphRun = new MutableGlyphRun();
        appendTextShape(createCursor(context), glyphRun, context);
        return glyphRun.shape();
    }

    @Override
    public void render(@NotNull RenderContext context, @NotNull Output output) {
        renderSegment(createCursor(context), context, output);
        if (DEBUG) {
            output.debugPaint(g -> paintDebugPath(context, g));
        }
    }

    private float computeStartOffset(@NotNull RenderContext context) {
        float offset = startOffset.resolve(context.measureContext());
        if (startOffset.unit().isPercentage()) {
            if (pathShape.isClosed(context)) {
                // Modulo 1 to obtain value inside [0, 1]
                offset = (offset % 1 + 1) % 1;
            }
            return (float) (offset * pathShape.pathLength(context));
        }
        return offset;
    }

    private @NotNull PathGlyphCursor createCursor(@NotNull RenderContext context) {
        return new PathGlyphCursor(
                createPathIterator(context),
                computeStartOffset(context));
    }

    private void paintDebugPath(@NotNull RenderContext context, @NotNull Graphics2D g) {
        PathIterator pathIterator = createPathIterator(context);
        float startX = 0;
        float startY = 0;
        float curX = 0;
        float curY = 0;
        g.setStroke(new BasicStroke(0.5f));
        float[] cord = new float[2];
        while (!pathIterator.isDone()) {
            switch (pathIterator.currentSegment(cord)) {
                case PathIterator.SEG_LINETO:
                    g.setColor(Color.MAGENTA);
                    g.draw(new Line2D.Float(curX, curY, cord[0], cord[1]));
                    g.setColor(Color.RED);
                    g.fillRect((int) curX - 2, (int) curY - 2, 4, 4);
                    g.fillRect((int) cord[0] - 2, (int) cord[1] - 2, 4, 4);
                    curX = cord[0];
                    curY = cord[1];
                    break;
                case PathIterator.SEG_MOVETO:
                    curX = cord[0];
                    curY = cord[1];
                    startX = curX;
                    startY = curY;
                    break;
                case PathIterator.SEG_CLOSE:
                    g.setColor(Color.MAGENTA);
                    g.draw(new Line2D.Float(curX, curY, startX, startY));
                    g.setColor(Color.RED);
                    g.fillRect((int) curX - 2, (int) curY - 2, 4, 4);
                    g.fillRect((int) startX - 2, (int) startY - 2, 4, 4);
                    curX = startX;
                    curY = startY;
                    break;
                default:
                    throw new IllegalStateException();
            }
            pathIterator.next();
        }
    }

    private @NotNull PathIterator createPathIterator(@NotNull RenderContext context) {
        MeasureContext measureContext = context.measureContext();
        Shape path = pathShape.shape(context);
        // For fonts this is a good enough approximation
        float flatness = 0.1f * measureContext.ex();
        switch (side) {
            case Left:
                return path.getPathIterator(null, flatness);
            case Right:
                return new ReversePathIterator(path.getPathIterator(null, flatness));
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    protected GlyphCursor createLocalCursor(@NotNull RenderContext context, @NotNull GlyphCursor current) {
        return new PathGlyphCursor(current,
                createPathIterator(context),
                computeStartOffset(context));
    }

    @Override
    protected void cleanUpLocalCursor(@NotNull GlyphCursor current, @NotNull GlyphCursor local) {
        current.updateFrom(local);
    }
}
