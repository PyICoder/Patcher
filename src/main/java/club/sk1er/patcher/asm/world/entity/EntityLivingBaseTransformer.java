/*
 * Copyright © 2020 by Sk1er LLC
 *
 * All rights reserved.
 *
 * Sk1er LLC
 * 444 S Fulton Ave
 * Mount Vernon, NY
 * sk1er.club
 */

package club.sk1er.patcher.asm.world.entity;

import club.sk1er.patcher.tweaker.transform.PatcherTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class EntityLivingBaseTransformer implements PatcherTransformer {
    /**
     * The class name that's being transformed
     *
     * @return the class name
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.entity.EntityLivingBase"};
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

            if (methodName.equals("getLook") || methodName.equals("func_70676_i")) {
                methodNode.instructions.insert(returnSpecial());
            } else if (methodName.equals("updatePotionEffects") || methodName.equals("func_70679_bo")) {
                int potionEffectIndex = -1;

                for (LocalVariableNode variable : methodNode.localVariables) {
                    if (variable.name.equals("potioneffect")) {
                        potionEffectIndex = variable.index;
                        break;
                    }
                }

                final ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    final AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode && next.getOpcode() == Opcodes.INVOKEINTERFACE && ((MethodInsnNode) next).name.equals("get")) {
                        methodNode.instructions.insertBefore(next.getNext().getNext().getNext(), checkPotionEffect(potionEffectIndex));
                    } else if (next instanceof FieldInsnNode) {
                        final String fieldName = mapFieldNameFromNode(next);
                        if (fieldName.equals("SPELL_MOB_AMBIENT")) {
                            methodNode.instructions.insertBefore(next.getPrevious().getPrevious().getPrevious().getPrevious(), checkCleanView());
                        }
                    }
                }
            }
        }
    }

    private InsnList checkCleanView() {
        InsnList list = new InsnList();
        list.add(getPatcherSetting("cleanView", "Z"));
        LabelNode label = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, label));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/client/Minecraft", "func_71410_x", "()Lnet/minecraft/client/Minecraft;", false));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/Minecraft", "field_71439_g", "Lnet/minecraft/client/entity/EntityPlayerSP;"));
        list.add(new JumpInsnNode(Opcodes.IF_ACMPNE, label));
        list.add(new InsnNode(Opcodes.RETURN));
        list.add(label);
        return list;
    }

    private InsnList checkPotionEffect(int potionEffectIndex) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, potionEffectIndex));
        LabelNode ifnonnull = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFNONNULL, ifnonnull));
        list.add(new InsnNode(Opcodes.RETURN));
        list.add(ifnonnull);
        return list;
    }

    private InsnList returnSpecial() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new TypeInsnNode(Opcodes.INSTANCEOF, "net/minecraft/client/entity/EntityPlayerSP"));
        LabelNode ifeq = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.FLOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/entity/Entity", "func_70676_i", // getLook
            "(F)Lnet/minecraft/util/Vec3;", false));
        list.add(new InsnNode(Opcodes.ARETURN));
        list.add(ifeq);
        return list;
    }
}