package com.creative.studio.component.dependency.compatibility;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.objectweb.asm.ClassReader;

import com.creative.studio.component.dependency.compatibility.CompatibleDetectingVisitor;

public class CompatibleDetectingVisitorTest {

    @Test
    public void simpleClassReaderTest() throws IOException {
        InputStream clazzStream = CompatibleDetectingVisitorTest.class
                .getResourceAsStream("/com/creative/studio/component/dependency/compatibility/CompatibleDetectingVisitor.class");
        ClassReader classReader = new ClassReader(clazzStream);
        CompatibleDetectingVisitor classVisitor = new CompatibleDetectingVisitor();
        classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
    }
}
