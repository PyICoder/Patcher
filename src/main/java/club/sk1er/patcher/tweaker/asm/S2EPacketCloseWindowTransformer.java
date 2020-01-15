package club.sk1er.patcher.tweaker.asm;

import club.sk1er.patcher.tweaker.transform.PatcherTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class S2EPacketCloseWindowTransformer implements PatcherTransformer {
    /**
     * The class name that's being transformed
     *
     * @return the class name
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.network.play.server.S2EPacketCloseWindow"};
    }

    /**
     * Perform any asm in order to transform code
     *
     * @param classNode the transformed class node
     * @param name      the transformed class name
     */
    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            String methodName = mapMethodName(classNode, methodNode);

            if (methodName.equals("processPacket") || methodName.equals("func_148833_a")) {
                methodNode.instructions.clear();
                methodNode.localVariables.clear();
                methodNode.instructions.insert(checkScreen());
            }
        }
    }

    private InsnList checkScreen() {
        InsnList list = new InsnList();
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, getPatcherConfigClass(), "stopForciblyClosingChat", "Z"));
        LabelNode ifne = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFNE, ifne));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/client/Minecraft", "func_71410_x", // getMinecraft
                "()Lnet/minecraft/client/Minecraft;", false));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/Minecraft", "field_71462_r", // currentScreen
                "Lnet/minecraft/client/gui/GuiScreen;"));
        list.add(new TypeInsnNode(Opcodes.INSTANCEOF, "net/minecraft/client/gui/GuiChat"));
        LabelNode ifne1 = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFNE, ifne1));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/network/play/INetHandlerPlayClient", "func_147276_a", // handleCloseWindow
                "(Lnet/minecraft/network/play/server/S2EPacketCloseWindow;)V", true));
        list.add(ifne);
        list.add(ifne1);
        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }
}