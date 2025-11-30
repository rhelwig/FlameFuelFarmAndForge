package com.ronhelwig.ffff.client.screen;

import static com.ronhelwig.ffff.FlameFuelFarmAndForge.MOD_ID;

import com.ronhelwig.ffff.menu.BronzeHopperMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BronzeHopperScreen extends AbstractContainerScreen<BronzeHopperMenu> {
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/container/small_hopper.png");

	public BronzeHopperScreen(BronzeHopperMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageHeight = 132;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			TEXTURE,
			x,
			y,
			0.0f,
			0.0f,
			this.imageWidth,
			this.imageHeight,
			256,
			256
		);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
		this.renderTooltip(graphics, mouseX, mouseY);
	}
}
