package com.carlmod.client.render;

import com.carlmod.CarlModClient;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class CarlEntityRenderer<T extends MobEntity> extends MobEntityRenderer<T, CarlEntityModel<T>> {
    private static final Identifier WILD_TEXTURE = new Identifier("carlmod", "textures/entity/wild_carl.png");
    private static final Identifier TAME_TEXTURE = new Identifier("carlmod", "textures/entity/tameable_carl.png");
    private final boolean isTameable;

    public CarlEntityRenderer(EntityRendererFactory.Context context, boolean isTameable) {
        super(context, new CarlEntityModel<>(context.getPart(CarlModClient.CARL_MODEL_LAYER)), 0.7F);
        this.isTameable = isTameable;
    }

    @Override
    public Identifier getTexture(T entity) {
        return isTameable ? TAME_TEXTURE : WILD_TEXTURE;
    }
}