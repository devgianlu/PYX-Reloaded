package net.socialgamer.cah;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

public class ConstantsTest {

    private static void crossCheck(Class firstEnum, Class secondEnum) throws NoSuchFieldException {
        for (Object firstEnumConst : firstEnum.getEnumConstants()) {
            for (Object secondEnumConst : secondEnum.getEnumConstants()) {
                if (shouldIgnoreDuplicate(firstEnumConst, secondEnumConst)) continue;
                Assertions.assertNotEquals(firstEnumConst.toString(), secondEnumConst.toString(),
                        "Found equal value '" + firstEnumConst.toString() + "' in " + firstEnum.getSimpleName() + " and " + secondEnum.getSimpleName());
            }
        }
    }

    public static boolean shouldIgnoreDuplicate(Object firstEnumConst, Object secondEnumConst) throws NoSuchFieldException {
        Class firstIgnoreClass = getIgnoreDuplicateClass(firstEnumConst);
        Class secondIgnoreClass = getIgnoreDuplicateClass(secondEnumConst);

        if (firstIgnoreClass == null && secondIgnoreClass == null) return false;

        if (firstIgnoreClass != null) {
            if (secondEnumConst.getClass() == firstIgnoreClass)
                return true;
        }

        return secondIgnoreClass != null && firstEnumConst.getClass() == secondIgnoreClass;

    }

    @Nullable
    public static Class getIgnoreDuplicateClass(Object enumConst) throws NoSuchFieldException {
        for (Annotation annotation : enumConst.getClass().getField(((Enum<?>) enumConst).name()).getAnnotations()) {
            if (annotation.annotationType() == Consts.IgnoreDuplicateIn.class)
                return ((Consts.IgnoreDuplicateIn) annotation).value();
        }

        return null;
    }

    @Test
    public void checkConstants() throws NoSuchFieldException {
        Class[] classes = Consts.class.getClasses();

        for (Class klass : classes) {
            for (Class kklass : classes) {
                if (klass.isEnum() && kklass.isEnum() && klass != kklass) crossCheck(klass, kklass);
            }
        }
    }
}
