package com.creative.studio.component.dependency.compatibility;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * @author <a href="mailto:fengjia10@gmail.com">Von Gosling</a>
 */
final class CompatibleDetectingVisitor extends ClassVisitor {
    private static final String     VOID              = "V";
    private final AnnotationVisitor annotationVisitor = new ClassDependencyAnnotationVisitor();
    private final FieldVisitor      fieldVisitor      = new ClassDependencyFieldVisitor();
    private final MethodVisitor     methodVisitor     = new ClassDependencyMethodVisitor();
    private final SignatureVisitor  signatureVisitor  = new ClassDependencySignatureVisitor();

    CompatibleDetectingVisitor() {
        super(Opcodes.ASM5);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
                      String[] interfaces) {
        if (superName != null) {
            final String superTypeName = readClassName(superName);
        }

        if (interfaces != null) {
            for (String iface : interfaces) {
                final String interfaceType = readClassName(iface);
            }
        }

        processSignature(signature);
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature,
                                   Object value) {
        final String fieldType = readTypeDescription(desc);

        // add initial field value if any
        if (value != null) {
            final String fieldValueType = getObjectValueType(value);
        }
        processSignature(signature);

        return fieldVisitor;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return delegateToAnnotationVisitor(desc);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        final String innerClassName = readClassName(name);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                     String[] exceptions) {
        final Type[] argumentTypes = Type.getArgumentTypes(desc);

        for (Type argumentType : argumentTypes) {
            final String parameterTypeName = readTypeName(argumentType);
        }

        final Type returnType = Type.getReturnType(desc);
        final String returnTypeName = readTypeName(returnType);

        if (exceptions != null) {
            for (String exception : exceptions) {
                final String exceptionName = readClassName(exception);
            }
        }

        processSignature(signature);

        return methodVisitor;
    }

    private AnnotationVisitor delegateToAnnotationVisitor(String desc) {
        final String annotationType = readTypeDescription(desc);

        return annotationVisitor;
    }

    private String readTypeDescription(String description) {
        final Type type = Type.getType(description);

        return readTypeName(type);
    }

    private String readClassName(String name) {
        final Type type = Type.getObjectType(name);
        return readTypeName(type);
    }

    private static String readTypeName(Type type) {
        switch (type.getSort()) {
            case Type.ARRAY: {
                return readTypeName(type.getElementType());
            }
            case Type.OBJECT: {
                return type.getClassName();
            }
            default: {
                return VOID;
            }
        }
    }

    private void processSignature(String signature) {
        if (signature != null) {
            final SignatureReader signatureReader = new SignatureReader(signature);
            signatureReader.accept(signatureVisitor);
        }
    }

    private final class ClassDependencySignatureVisitor extends SignatureVisitor {
        private ClassDependencySignatureVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visitClassType(String name) {
            final String classType = readClassName(name);
        }
    }

    private final class ClassDependencyAnnotationVisitor extends AnnotationVisitor {
        private ClassDependencyAnnotationVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(String name, Object value) {
            final String valueType = getObjectValueType(value);
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            final String enumType = readTypeDescription(desc);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            final String annotationType = readTypeDescription(desc);

            return this;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return this;
        }
    }

    private static String getObjectValueType(Object value) {
        String valueType;
        if (value instanceof Type) {
            final Type type = (Type) value;
            valueType = readTypeName(type);
        } else {
            valueType = value.getClass().getCanonicalName();
        }
        return valueType;
    }

    private final class ClassDependencyFieldVisitor extends FieldVisitor {
        private ClassDependencyFieldVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return delegateToAnnotationVisitor(desc);
        }
    }

    private final class ClassDependencyMethodVisitor extends MethodVisitor {
        private ClassDependencyMethodVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return annotationVisitor;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return delegateToAnnotationVisitor(desc);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc,
                                                          boolean visible) {
            return delegateToAnnotationVisitor(desc);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            final String typeName = readClassName(type);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            final String fieldType = readTypeDescription(desc);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            final String ownerType = readClassName(owner);

            final Type[] argumentTypes = Type.getArgumentTypes(desc);

            for (Type argumentType : argumentTypes) {
                final String parameterTypeName = readTypeName(argumentType);
            }

            final Type returnType = Type.getReturnType(desc);
            final String returnTypeName = readTypeName(returnType);
        }

        @Override
        public void visitLdcInsn(Object cst) {
            final String constantTypeName = getObjectValueType(cst);
        }

        @Override
        public void visitMultiANewArrayInsn(String desc, int dims) {
            final String arrayType = readTypeDescription(desc);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            if (type != null) {
                final String exceptionType = readClassName(type);
            }
        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start,
                                       Label end, int index) {
            final String localVariableType = readTypeDescription(desc);

            processSignature(signature);
        }
    }
}
