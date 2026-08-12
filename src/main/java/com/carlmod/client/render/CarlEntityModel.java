package com.carlmod.client.render;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.mob.MobEntity;

public class CarlEntityModel<T extends MobEntity> extends SinglePartEntityModel<T> {
    private final ModelPart root;

    public CarlEntityModel(ModelPart root) {
        this.root = root;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        
        // 创建一个 16x16x16 方块大小的基础大嘴主体模型
        modelPartData.addChild("body", ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                Transform.NONE);
                
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // 肢体动画预留位
    }
}