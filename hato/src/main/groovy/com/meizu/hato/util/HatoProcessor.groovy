package com.meizu.hato.util

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Created by jixin.jia on 15/11/10.
 */
class HatoProcessor {
    public
    static processJar(File hashFile, File jarFile, File patchDir, Map map, HashSet<String> includePackage, HashSet<String> excludeClass) {
        if (jarFile) {
            def optJar = new File(jarFile.getParent(), jarFile.name + ".opt")
            def file = new JarFile(jarFile);
            Enumeration enumeration = file.entries();
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar));

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                String entryName = jarEntry.getName();
                ZipEntry zipEntry = new ZipEntry(entryName);

                InputStream inputStream = file.getInputStream(jarEntry);
                jarOutputStream.putNextEntry(zipEntry);

                if (shouldProcessClassInJar(entryName, includePackage, excludeClass)) {
                    def bytes = referHackWhenInit(inputStream);
                    jarOutputStream.write(bytes);

                    def hash = DigestUtils.shaHex(bytes)
                    hashFile.append(HatoMapUtils.format(entryName, hash))

                    //copy modified class to patchDir
                    if (HatoMapUtils.notSame(map, entryName, hash)) {
                        HatoFileUtils.copyBytesToFile(bytes, HatoFileUtils.touchFile(patchDir, entryName))
                    }
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream));
                }
                jarOutputStream.closeEntry();
            }
            jarOutputStream.close();
            file.close();

            if (jarFile.exists()) {
                jarFile.delete()
            }
            optJar.renameTo(jarFile)
        }

    }

    //refer hack class when object init
    private static byte[] referHackWhenInit(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new InjectCassVisitor(Opcodes.ASM4, cw);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    public static boolean shouldProcessPreDexJar(String path) {
        return path.endsWith("classes.jar") && !path.contains("com.android.support") && !path.contains("/android/m2repository");
    }

    private
    static boolean shouldProcessClassInJar(String entryName, HashSet<String> includePackage, HashSet<String> excludeClass) {
        if (!entryName.endsWith(".class")) {
            return false;
        }
        if (entryName.contains("/R\$") || entryName.endsWith("/R.class") || entryName.endsWith("/BuildConfig.class") || entryName.startsWith("com/meizu/hato/") || entryName.contains("android/support/")){
            return false;
        }
        return HatoSetUtils.isIncluded(entryName, includePackage) && !HatoSetUtils.isExcluded(entryName, excludeClass)
    }

    public static byte[] processClass(File file) {
        def optClass = new File(file.getParent(), file.name + ".opt")

        FileInputStream inputStream = new FileInputStream(file);
        FileOutputStream outputStream = new FileOutputStream(optClass)

        def bytes = referHackWhenInit(inputStream);
        outputStream.write(bytes)
        inputStream.close()
        outputStream.close()
        if (file.exists()) {
            file.delete()
        }
        optClass.renameTo(file)
        return bytes
    }

    public static void processClass(File inputFile, File hashFile, Map hashMap, File patchDir, String dirName, HashSet<String> includePackage, HashSet<String> excludeClass) {
        def path = inputFile.absolutePath
        if (shouldProcessClassInJar(path, includePackage, excludeClass)){
            def bytes = HatoProcessor.processClass(inputFile)
            path = path.split("${dirName}/")[1]
            def hash = DigestUtils.shaHex(bytes)
            hashFile.append(HatoMapUtils.format(path, hash))

            if (HatoMapUtils.notSame(hashMap, path, hash)) {
                //copy insame class to patch dir
                HatoFileUtils.copyBytesToFile(inputFile.bytes, HatoFileUtils.touchFile(patchDir, path))
            }

        }
    }

    static class InjectCassVisitor extends ClassVisitor {
        InjectCassVisitor(int i, ClassVisitor classVisitor) {
            super(i, classVisitor)
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {

            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            mv = new MethodVisitor(Opcodes.ASM4, mv) {
                @Override
                void visitInsn(int opcode) {
                    if ("<init>".equals(name) && opcode == Opcodes.RETURN) {
                        Label l1 = new Label();
                        super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                        super.visitJumpInsn(Opcodes.IFEQ, l1);
                        super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                        super.visitLdcInsn(Type.getType("Lcn/jiajixin/nuwa/Hack;"));
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
                        super.visitLabel(l1);
                    }
                    super.visitInsn(opcode);
                }

                @Override
                public void visitMaxs(int maxStack, int maxLocal) {
                    if ("<init>".equals(name)) {
                        super.visitMaxs(maxStack + 2, maxLocal);
                    } else {
                        super.visitMaxs(maxStack, maxLocal);
                    }
                }
            }
            return mv;
        }
    }

}
