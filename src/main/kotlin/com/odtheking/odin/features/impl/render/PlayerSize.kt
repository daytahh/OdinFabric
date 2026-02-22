package com.odtheking.odin.features.impl.render

import com.mojang.authlib.GameProfile
import com.mojang.blaze3d.vertex.PoseStack
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.features.Module
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
import net.minecraft.client.renderer.entity.state.AvatarRenderState

object PlayerSize : Module(
    name = "Player Size",
    description = "Changes the size of the player."
) {
    private val devSizeX by NumberSetting("Size X", 1f, -1, 3f, 0.1, desc = "X scale of the dev size.")
    private val devSizeY by NumberSetting("Size Y", 1f, -1, 3f, 0.1, desc = "Y scale of the dev size.")
    private val devSizeZ by NumberSetting("Size Z", 1f, -1, 3f, 0.1, desc = "Z scale of the dev size.")

    @JvmStatic
    fun preRenderCallbackScaleHook(entityRenderer: AvatarRenderState, matrix: PoseStack) {
        val gameProfile = entityRenderer.getData(GAME_PROFILE_KEY) ?: return
        if (enabled && gameProfile.name == mc.player?.gameProfile?.name) {
            if (devSizeY < 0) matrix.translate(0f, devSizeY * 2, 0f)
            matrix.scale(devSizeX, devSizeY, devSizeZ)
        }
    }

    @JvmStatic
    val GAME_PROFILE_KEY: RenderStateDataKey<GameProfile> = RenderStateDataKey.create { "odin:game_profile" }
}