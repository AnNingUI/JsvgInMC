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
package com.github.weisj.jsvg_mc.renderer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.weisj.jsvg_mc.attributes.FillRule;
import com.github.weisj.jsvg_mc.attributes.PaintOrder;
import com.github.weisj.jsvg_mc.attributes.ViewBox;
import com.github.weisj.jsvg_mc.attributes.font.FontResolver;
import com.github.weisj.jsvg_mc.attributes.font.MeasurableFontSpec;
import com.github.weisj.jsvg_mc.attributes.font.SVGFont;
import com.github.weisj.jsvg_mc.attributes.paint.PredefinedPaints;
import com.github.weisj.jsvg_mc.attributes.paint.SVGPaint;
import com.github.weisj.jsvg_mc.attributes.stroke.StrokeResolver;
import com.github.weisj.jsvg_mc.geometry.size.MeasureContext;
import com.github.weisj.jsvg_mc.nodes.prototype.Mutator;
import com.github.weisj.jsvg_mc.renderer.awt.PlatformSupport;

public final class RenderContext {

    private final @NotNull PlatformSupport awtSupport;
    private final @NotNull MeasureContext measureContext;
    private final @NotNull PaintContext paintContext;

    private final @NotNull FontRenderContext fontRenderContext;
    private final @NotNull MeasurableFontSpec fontSpec;


    private final @Nullable ContextElementAttributes contextElementAttributes;

    private final @NotNull AffineTransform rootTransform;
    private final @NotNull AffineTransform userSpaceTransform;


    public static @NotNull RenderContext createInitial(@NotNull PlatformSupport awtSupport,
            @NotNull MeasureContext measureContext) {
        return new RenderContext(awtSupport,
                new AffineTransform(),
                new AffineTransform(),
                PaintContext.createDefault(),
                measureContext,
                FontRenderContext.createDefault(),
                MeasurableFontSpec.createDefault(),
                null);
    }

    RenderContext(@NotNull PlatformSupport platformSupport,
            @NotNull AffineTransform rootTransform,
            @NotNull AffineTransform userSpaceTransform,
            @NotNull PaintContext paintContext,
            @NotNull MeasureContext measureContext,
            @NotNull FontRenderContext fontRenderContext,
            @NotNull MeasurableFontSpec fontSpec,
            @Nullable ContextElementAttributes contextElementAttributes) {
        this.awtSupport = platformSupport;
        this.rootTransform = rootTransform;
        this.userSpaceTransform = userSpaceTransform;
        this.paintContext = paintContext;
        this.measureContext = measureContext;
        this.fontRenderContext = fontRenderContext;
        this.fontSpec = fontSpec;
        this.contextElementAttributes = contextElementAttributes;
    }

    @NotNull
    RenderContext derive(
            @Nullable Mutator<PaintContext> context,
            @Nullable Mutator<MeasurableFontSpec> attributeFontSpec,
            @Nullable ViewBox viewBox, @Nullable FontRenderContext frc,
            @Nullable ContextElementAttributes contextAttributes,
            EstablishRootMeasure establishRootMeasure) {
        return deriveImpl(context, attributeFontSpec, viewBox, frc, contextAttributes, null,
                establishRootMeasure);
    }

    public enum EstablishRootMeasure {
        Yes,
        No
    }

    @NotNull
    private RenderContext deriveImpl(@Nullable Mutator<PaintContext> context,
            @Nullable Mutator<MeasurableFontSpec> attributeFontSpec,
            @Nullable ViewBox viewBox, @Nullable FontRenderContext frc,
            @Nullable ContextElementAttributes contextAttributes,
            @Nullable AffineTransform rootTransform,
            EstablishRootMeasure establishRootMeasure) {
        if (context == null && viewBox == null && attributeFontSpec == null && frc == null) return this;
        PaintContext newPaintContext = paintContext;
        MeasurableFontSpec newFontSpec = fontSpec;

        if (context != null) newPaintContext = context.mutate(paintContext);
        if (attributeFontSpec != null) newFontSpec = attributeFontSpec.mutate(newFontSpec);

        ContextElementAttributes newContextAttributes = contextElementAttributes;
        if (contextAttributes != null) newContextAttributes = contextAttributes;

        float em = newFontSpec.effectiveSize(measureContext);
        float ex = SVGFont.exFromEm(em);
        MeasureContext newMeasureContext = measureContext.derive(viewBox, em, ex);

        if (establishRootMeasure == EstablishRootMeasure.Yes) {
            newMeasureContext = newMeasureContext.deriveRoot(em);
        }

        FontRenderContext effectiveFrc = fontRenderContext.derive(frc);
        AffineTransform newRootTransform = rootTransform != null ? rootTransform : this.rootTransform;

        return new RenderContext(awtSupport, newRootTransform, new AffineTransform(userSpaceTransform),
                newPaintContext, newMeasureContext, effectiveFrc, newFontSpec, newContextAttributes);
    }

    public @NotNull RenderContext deriveForChildGraphics() {
        // Pass non-trivial context mutator to ensure userSpaceTransform gets created a different copy.
        return derive(t -> t, null, null, null, null, EstablishRootMeasure.No);
    }

    public @NotNull RenderContext deriveForSurface() {
        return deriveImpl(t -> t, null, null, null, null,
                new AffineTransform(rootTransform), EstablishRootMeasure.No);
    }

    public @NotNull StrokeContext strokeContext() {
        // This will never be null for a RenderContext.
        // Our deriving mechanism together with non-null initial values prohibits this.
        assert paintContext.strokeContext != null;
        return paintContext.strokeContext;
    }

    @Nullable
    ContextElementAttributes contextElementAttributes() {
        return contextElementAttributes;
    }

    public @NotNull AffineTransform rootTransform() {
        return rootTransform;
    }

    public @NotNull AffineTransform userSpaceTransform() {
        return userSpaceTransform;
    }

    public void setRootTransform(@NotNull AffineTransform rootTransform) {
        this.rootTransform.setTransform(rootTransform);
        this.userSpaceTransform.setToIdentity();
    }

    public void setRootTransform(@NotNull AffineTransform rootTransform, @NotNull AffineTransform userSpaceTransform) {
        this.rootTransform.setTransform(rootTransform);
        this.userSpaceTransform.setTransform(userSpaceTransform);
    }

    public void translate(@NotNull Output output, @NotNull Point2D dp) {
        translate(output, dp.getX(), dp.getY());
    }

    public void translate(@NotNull Output output, double dx, double dy) {
        output.translate(dx, dy);
        userSpaceTransform.translate(dx, dy);
    }

    public void scale(@NotNull Output output, double sx, double sy) {
        output.scale(sx, sy);
        userSpaceTransform.scale(sx, sy);
    }

    public void rotate(@NotNull Output output, double angle) {
        output.rotate(angle);
        userSpaceTransform.rotate(angle);
    }

    public void transform(@NotNull Output output, @NotNull AffineTransform at) {
        output.applyTransform(at);
        userSpaceTransform.concatenate(at);
    }

    public @NotNull PlatformSupport platformSupport() {
        return awtSupport;
    }

    public @NotNull MeasureContext measureContext() {
        return measureContext;
    }

    public @NotNull FontRenderContext fontRenderContext() {
        return fontRenderContext;
    }

    public @NotNull FillRule fillRule() {
        FillRule fillRule = paintContext.fillRule;
        return fillRule != null ? fillRule : FillRule.Nonzero;
    }

    public @NotNull SVGPaint strokePaint() {
        return resolvePaint(paintContext.strokePaint);
    }

    public @NotNull SVGPaint fillPaint() {
        return resolvePaint(paintContext.fillPaint);
    }

    public @NotNull PaintOrder paintOrder() {
        PaintOrder paintOrder = paintContext.paintOrder;
        return paintOrder != null ? paintOrder : PaintOrder.NORMAL;
    }

    private @NotNull SVGPaint resolvePaint(@Nullable SVGPaint p) {
        if (p == PredefinedPaints.DEFAULT_PAINT || p == PredefinedPaints.CURRENT_COLOR) {
            // color can only hold resolved values being declared as literals
            return coerceNonNull(paintContext.color);
        }
        if (p == PredefinedPaints.CONTEXT_STROKE) {
            // value is already absolute hence needs no special treatment.
            if (contextElementAttributes == null) return PredefinedPaints.NONE;
            return contextElementAttributes.strokePaint;
        }
        if (p == PredefinedPaints.CONTEXT_FILL) {
            // value is already absolute hence needs no special treatment.
            if (contextElementAttributes == null) return PredefinedPaints.NONE;
            return contextElementAttributes.fillPaint;
        }
        return coerceNonNull(p);
    }

    private @NotNull SVGPaint coerceNonNull(@Nullable SVGPaint p) {
        return p != null ? p : PredefinedPaints.DEFAULT_PAINT;
    }

    public float rawOpacity() {
        return paintContext.opacity.get(measureContext);
    }

    public float fillOpacity() {
        assert paintContext.fillOpacity != null;
        return paintContext.fillOpacity.get(measureContext) * paintContext.opacity.get(measureContext);
    }

    public float strokeOpacity() {
        assert paintContext.strokeOpacity != null;
        return paintContext.strokeOpacity.get(measureContext) * paintContext.opacity.get(measureContext);
    }

    public @NotNull Stroke stroke(float pathLengthFactor) {
        return StrokeResolver.resolve(pathLengthFactor, measureContext, strokeContext());
    }

    public @NotNull SVGFont font() {
        return FontResolver.resolve(this.fontSpec, this.measureContext);
    }

    @Override
    public String toString() {
        return "RenderContext{" +
                "\n  measureContext=" + measureContext +
                ",\n paintContext=" + paintContext +
                ",\n fontSpec=" + fontSpec +
                ",\n awtSupport=" + awtSupport +
                ",\n contextElementAttributes=" + contextElementAttributes +
                ",\n baseTransform=" + rootTransform +
                ",\n userSpaceTransform=" + userSpaceTransform +
                "\n}";
    }
}
