package com.github.fusion.client.render;

import com.github.fusion.FusionMod;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.NotNull;

public final class SkinCompositor {

    private SkinCompositor() {} 

    @NotNull
    public static NativeImage compositeSideBySide(@NotNull NativeImage skinA, @NotNull NativeImage skinB) {
        int width = 64;
        int height = 64;
        NativeImage result = new NativeImage(width, height, true);

        int halfWidth = width / 2;

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < halfWidth; x++) {
                result.setPixel(x, y, skinA.getPixel(x, y));
            }

            for (int x = halfWidth; x < width; x++) {
                result.setPixel(x, y, skinB.getPixel(x, y));
            }
        }

        return result;
    }

    @NotNull
    public static NativeImage compositeBackToBack(@NotNull NativeImage skinA, @NotNull NativeImage skinB) {

        NativeImage result = new NativeImage(64, 64, true);

        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                result.setPixel(x, y, skinA.getPixel(x, y));
            }
        }

        copyRegion(skinB, result, 24, 8, 24, 8, 8, 8);

        copyRegion(skinB, result, 32, 20, 32, 20, 8, 12);

        copyRegion(skinB, result, 52, 20, 52, 20, 4, 12);

        copyRegion(skinB, result, 44, 20, 44, 20, 4, 12);

        copyRegion(skinB, result, 28, 52, 28, 52, 4, 12);

        copyRegion(skinB, result, 12, 20, 12, 20, 4, 12);

        return result;
    }

    private static void copyRegion(NativeImage src, NativeImage dst,
                                    int srcX, int srcY, int dstX, int dstY,
                                    int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                dst.setPixel(dstX + x, dstY + y, src.getPixel(srcX + x, srcY + y));
            }
        }
    }
}
