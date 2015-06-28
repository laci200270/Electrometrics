package org.halvors.electrometrics.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.halvors.electrometrics.client.gui.element.GuiElement;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Gui extends GuiContainer implements IGui {
    private TileEntity tileEntity;

    private Set<GuiElement> guiElementList = new HashSet<GuiElement>();

    public Gui(TileEntity tile, Container container) {
        super(container);

        tileEntity = tile;
    }

    public void add(GuiElement guiElement) {
        guiElementList.add(guiElement);
    }

    public float getNeededScale(String text, int maxX) {
        int length = fontRendererObj.getStringWidth(text);

        if (length <= maxX) {
            return 1;
        } else {
            return (float) maxX / length;
        }
    }

    /** returns scale */
    public void renderScaledText(String text, int x, int y, int color, int maxX) {
        int length = fontRendererObj.getStringWidth(text);

        if (length <= maxX) {
            fontRendererObj.drawString(text, x, y, color);
        } else {
            float scale = (float )maxX / length;
            float reverse = 1 / scale;
            float yAdd = 4 - (scale * 8) / 2F;

            GL11.glPushMatrix();

            GL11.glScalef(scale, scale, scale);
            fontRendererObj.drawString(text, (int) (x * reverse), (int) ((y * reverse) + yAdd), color);

            GL11.glPopMatrix();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);

        for (GuiElement element : guiElementList) {
            element.renderForeground(xAxis, yAxis);
        }

        /*
        if (tileEntity instanceof ISideConfiguration) {
            Slot hovering = null;

            for (int i = 0; i < inventorySlots.inventorySlots.size(); i++) {
                Slot slot = (Slot)inventorySlots.inventorySlots.get(i);

                if (isMouseOverSlot(slot, mouseX, mouseY)) {
                    hovering = slot;
                    break;
                }
            }

            ItemStack stack = mc.thePlayer.inventory.getItemStack();

            if (stack != null && stack.getItem() instanceof ItemConfigurator && hovering != null) {
                SideData data = getFromSlot(hovering);

                if (data != null) {
                    drawCreativeTabHoveringText(data.color + data.localize() + " (" + data.color.getName() + ")", xAxis, yAxis);
                }
            }
        }
        */
    }

    /*
    private SideData getFromSlot(Slot slot) {
        if (slot.slotNumber < tileEntity.getSizeInventory()) {
            ISideConfiguration config = (ISideConfiguration)tileEntity;

            for (SideData data : config.getConfig().getOutputs(TransmissionType.ITEM)) {
                for (int id : data.availableSlots) {
                    if (id == slot.getSlotIndex()) {
                        return data;
                    }
                }
            }
        }

        return null;
    }
    */

    protected boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
        return func_146978_c(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY); // isPointInRegion
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;

        int xAxis = mouseX - guiWidth;
        int yAxis = mouseY - guiHeight;

        for (GuiElement element : guiElementList) {
            element.renderBackground(xAxis, yAxis, guiWidth, guiHeight);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);

        for (GuiElement element : guiElementList) {
            element.preMouseClicked(xAxis, yAxis, button);
        }

        super.mouseClicked(mouseX, mouseY, button);

        for (GuiElement element : guiElementList) {
            element.mouseClicked(xAxis, yAxis, button);
        }
    }

    @Override
    protected void drawCreativeTabHoveringText(String text, int x, int y) {
        func_146283_a(Arrays.asList(new String[]{text}), x, y);
    }

    @Override
    protected void func_146283_a(List list, int x, int y) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT + GL11.GL_LIGHTING_BIT);
        super.func_146283_a(list, x, y);
        GL11.glPopAttrib();
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long ticks) {
        super.mouseClickMove(mouseX, mouseY, button, ticks);

        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);

        for (GuiElement element : guiElementList) {
            element.mouseClickMove(xAxis, yAxis, button, ticks);
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int type) {
        super.mouseMovedOrUp(mouseX, mouseY, type);

        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);

        for (GuiElement element : guiElementList) {
            element.mouseMovedOrUp(xAxis, yAxis, type);
        }
    }

    public void handleMouse(Slot slot, int slotIndex, int button, int modifier) {
        handleMouseClick(slot, slotIndex, button, modifier);
    }

    @Override
    public void drawTexturedRect(int x, int y, int u, int v, int w, int h) {
        drawTexturedModalRect(x, y, u, v, w, h);
    }

    @Override
    public void drawTexturedRectFromIcon(int x, int y, IIcon icon, int w, int h) {
        drawTexturedModelRectFromIcon(x, y, icon, w, h);
    }

    @Override
    public void displayTooltip(String text, int x, int y) {
        drawCreativeTabHoveringText(text, x, y);
    }

    @Override
    public void displayTooltips(List<String> list, int xAxis, int yAxis) {
        func_146283_a(list, xAxis, yAxis);
    }

    @Override
    public FontRenderer getFontRenderer() {
        return fontRendererObj;
    }
}