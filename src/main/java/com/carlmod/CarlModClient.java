package com.carlmod;

import com.carlmod.client.render.CarlEntityModel;
import com.carlmod.client.render.CarlEntityRenderer;
import com.carlmod.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class CarlModClient implements ClientModInitializer {
    public static final EntityModelLayer CARL_MODEL_LAYER =
            new EntityModelLayer(new Identifier("carlmod", "carl"), "main");

    @Override
    public void onInitializeClient() {
        // 1. 注册实体模型图层
        EntityModelLayerRegistry.registerModelLayer(CARL_MODEL_LAYER, CarlEntityModel::getTexturedModelData);

        // 2. 为野生与可驯服 Carl 绑定渲染器
        EntityRendererRegistry.register(ModEntities.WILD_CARL, ctx -> new CarlEntityRenderer<>(ctx, false));
        EntityRendererRegistry.register(ModEntities.TAMEABLE_CARL, ctx -> new CarlEntityRenderer<>(ctx, true));
    }
}