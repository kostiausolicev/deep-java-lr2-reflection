package org.example.generator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorTest {
    private final Generator generator = new Generator();

    @Test
    void generatesPrimitiveTypesAndTheirWrappers() throws Exception {
        Map<Class<?>, Class<?>> expectedResultTypes = Map.ofEntries(
                Map.entry(int.class, Integer.class),
                Map.entry(Integer.class, Integer.class),
                Map.entry(long.class, Long.class),
                Map.entry(Long.class, Long.class),
                Map.entry(float.class, Float.class),
                Map.entry(Float.class, Float.class),
                Map.entry(double.class, Double.class),
                Map.entry(Double.class, Double.class),
                Map.entry(char.class, Character.class),
                Map.entry(Character.class, Character.class),
                Map.entry(boolean.class, Boolean.class),
                Map.entry(Boolean.class, Boolean.class),
                Map.entry(short.class, Short.class),
                Map.entry(Short.class, Short.class),
                Map.entry(byte.class, Byte.class),
                Map.entry(Byte.class, Byte.class)
        );

        for (Map.Entry<Class<?>, Class<?>> entry : expectedResultTypes.entrySet()) {
            Object generated = generator.generateValueOfType(entry.getKey());

            assertNotNull(generated);
            assertEquals(entry.getValue(), generated.getClass());
        }
    }

    @Test
    void generatesNonEmptyStringFromLowercaseLatinLetters() throws Exception {
        Object generated = generator.generateValueOfType(String.class);

        String value = assertInstanceOf(String.class, generated);
        assertTrue(value.matches("[a-z]{5,19}"));
    }

    @Test
    void generatesOneOfEnumConstants() throws Exception {
        Object generated = generator.generateValueOfType(TestColor.class);

        TestColor color = assertInstanceOf(TestColor.class, generated);
        assertTrue(List.of(TestColor.values()).contains(color));
    }

    @Test
    void generatesPrimitiveArrayWithGeneratedElements() throws Exception {
        Object generated = generator.generateValueOfType(int[].class);

        int[] values = assertInstanceOf(int[].class, generated);
        assertTrue(values.length >= 1 && values.length <= 4);
    }

    @Test
    void generatesReferenceTypeArrayWithGeneratedElements() throws Exception {
        Object generated = generator.generateValueOfType(String[].class);

        String[] values = assertInstanceOf(String[].class, generated);
        assertTrue(values.length >= 1 && values.length <= 4);
        for (String value : values) {
            assertNotNull(value);
            assertTrue(value.matches("[a-z]{5,19}"));
        }
    }

    @Test
    void generatesNonEmptyListOfStrings() throws Exception {
        Object generated = generator.generateValueOfType(List.class);

        List<?> values = assertInstanceOf(List.class, generated);
        assertTrue(values.size() >= 1 && values.size() <= 4);
        for (Object value : values) {
            String string = assertInstanceOf(String.class, value);
            assertTrue(string.matches("[a-z]{5,19}"));
        }
    }

    @Test
    void generatesNonEmptyMapWithStringKeysAndIntegerValues() throws Exception {
        Object generated = generator.generateValueOfType(Map.class);

        Map<?, ?> values = assertInstanceOf(Map.class, generated);
        assertTrue(values.size() >= 1 && values.size() <= 4);
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            String key = assertInstanceOf(String.class, entry.getKey());
            assertTrue(key.matches("[a-z]{5,19}"));
            assertInstanceOf(Integer.class, entry.getValue());
        }
    }

    private enum TestColor {
        RED,
        GREEN,
        BLUE
    }
}
